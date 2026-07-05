package ro.facultate.pos.service;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.server.ResponseStatusException;
import ro.facultate.pos.dto.CreateCategorieRequest;
import ro.facultate.pos.dto.UpdateCategorieRequest;
import ro.facultate.pos.entity.Categorie;
import ro.facultate.pos.repository.CategorieRepository;
import ro.facultate.pos.repository.ProdusRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class CategorieServiceTest {

    private final CategorieRepository categorieRepository = Mockito.mock(CategorieRepository.class);
    private final ProdusRepository produsRepository = Mockito.mock(ProdusRepository.class);
    private final CategorieService categorieService = new CategorieService(categorieRepository, produsRepository);

    @Test
    void create_shouldSaveCategorie() {
        CreateCategorieRequest req = new CreateCategorieRequest();
        req.setNume("Panificatie");

        Categorie saved = new Categorie();
        saved.setId(1L);
        saved.setNume("Panificatie");

        Mockito.when(categorieRepository.save(Mockito.any(Categorie.class))).thenReturn(saved);

        Categorie result = categorieService.create(req);

        assertEquals(1L, result.getId());
        assertEquals("Panificatie", result.getNume());

        ArgumentCaptor<Categorie> captor = ArgumentCaptor.forClass(Categorie.class);
        Mockito.verify(categorieRepository).save(captor.capture());
        assertEquals("Panificatie", captor.getValue().getNume());
    }

    @Test
    void getAll_shouldReturnList() {
        Mockito.when(categorieRepository.findAll()).thenReturn(List.of(new Categorie()));

        List<Categorie> result = categorieService.getAll();

        assertEquals(1, result.size());
        Mockito.verify(categorieRepository).findAll();
    }

    @Test
    void getById_notFound_throwsNotFound() {
        Mockito.when(categorieRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categorieService.getById(99L));

        assertEquals(404, ex.getStatusCode().value());
    }

    @Test
    void update_shouldUpdateNume() {
        Categorie existing = new Categorie();
        existing.setId(1L);
        existing.setNume("Panificatie");

        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(categorieRepository.save(Mockito.any(Categorie.class))).thenAnswer(inv -> inv.getArgument(0));

        UpdateCategorieRequest req = new UpdateCategorieRequest();
        req.setNume("Panificatie si patiserie");

        Categorie result = categorieService.update(1L, req);

        assertEquals("Panificatie si patiserie", result.getNume());
    }

    @Test
    void delete_shouldRemoveCategorie() {
        Categorie existing = new Categorie();
        existing.setId(1L);

        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(produsRepository.existsByCategorieId(1L)).thenReturn(false);

        categorieService.delete(1L);

        Mockito.verify(categorieRepository).delete(existing);
    }

    @Test
    void delete_withProduse_throwsBadRequest() {
        Categorie existing = new Categorie();
        existing.setId(1L);

        Mockito.when(categorieRepository.findById(1L)).thenReturn(Optional.of(existing));
        Mockito.when(produsRepository.existsByCategorieId(1L)).thenReturn(true);

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> categorieService.delete(1L));

        assertEquals(400, ex.getStatusCode().value());
    }
}
