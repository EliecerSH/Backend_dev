package cl.proyectoEcommerce.ms_carrito.models;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "carritos")
public class Carrito {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "usuario_oid", nullable = false, unique = true)
    private String usuarioOid;

    @OneToMany(mappedBy = "carrito", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CarritoItem> items = new ArrayList<>();

    @Column(name = "creado_en", updatable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public Carrito() {}

    public Carrito(String usuarioOid) {
        this.usuarioOid = usuarioOid;
    }

    @PrePersist
    protected void onCreate() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }

    // Helper methods para gestión bidireccional
    public void agregarItem(CarritoItem item) {
        items.add(item);
        item.setCarrito(this);
    }

    public void removerItem(CarritoItem item) {
        items.remove(item);
        item.setCarrito(null);
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsuarioOid() { return usuarioOid; }
    public void setUsuarioOid(String usuarioOid) { this.usuarioOid = usuarioOid; }

    public List<CarritoItem> getItems() { return items; }
    public void setItems(List<CarritoItem> items) { this.items = items; }

    public LocalDateTime getCreadoEn() { return creadoEn; }
    public LocalDateTime getActualizadoEn() { return actualizadoEn; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Carrito carrito = (Carrito) o;
        return Objects.equals(id, carrito.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
