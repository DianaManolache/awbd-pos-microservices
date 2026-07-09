package ro.facultate.pos.config;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.cache.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

/**
 * Entitatile JPA (Categorie, Produs, Promotie) nu implementeaza
 * Serializable, deci serializarea implicita JDK a Redis ar arunca
 * NotSerializableException. Foloseste JSON (Jackson) in schimb.
 * Constructorul fara argumente al GenericJackson2JsonRedisSerializer isi
 * construieste propriul ObjectMapper, izolat, si il configureaza corect
 * cu type-info polimorfic ("@class") - necesar ca Spring @Cacheable sa
 * poata reconstitui tipul concret exact (ex. Produs, nu un LinkedHashMap
 * generic) la citire din cache, mai ales pentru valori unice (getById),
 * nu doar liste. Incercarea anterioara de a inlocui acest mapper cu unul
 * complet nou, fara typing configurat manual, a facut ca getById() sa
 * primeasca LinkedHashMap in loc de Produs la cache hit
 * (ClassCastException) - typing-ul polimorfic nu se activeaza singur
 * doar pentru ca dai constructorului un ObjectMapper oarecare.
 * `.configure(...)` permite adaugarea JavaTimeModule (necesar pentru
 * Promotie.dataStart/dataFinal) DUPA ce serializatorul si-a configurat
 * deja corect propriul mapper intern, fara sa afecteze ObjectMapper-ul
 * partajat folosit de raspunsurile REST (mapper-ul intern e complet
 * izolat, nu bean-ul Spring).
 */
@Configuration
public class CacheConfig {

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer() {
        GenericJackson2JsonRedisSerializer serializer = new GenericJackson2JsonRedisSerializer()
                .configure(mapper -> mapper.registerModule(new JavaTimeModule()));
        return builder -> builder.cacheDefaults(
                RedisCacheConfiguration.defaultCacheConfig()
                        .serializeValuesWith(RedisSerializationContext.SerializationPair
                                .fromSerializer(serializer))
        );
    }
}
