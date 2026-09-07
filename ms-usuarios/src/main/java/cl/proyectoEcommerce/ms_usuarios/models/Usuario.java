package cl.proyectoEcommerce.ms_usuarios.models;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // Campo obligatorio para vincular con Azure AD (OID o SUB)
    @Column(name = "azure_oid", unique = true)
    private String azureOid;

    private String nombre;
    private String direccion;
    private String comuna;
    private String region;
    private String ciudad;
    private String rol;

    // Getter y Setter para azureOid
    public String getAzureOid() { return azureOid; }
    public void setAzureOid(String azureOid) { this.azureOid = azureOid; }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public String getCiudad() {
        return ciudad;
    }

    public void setCiudad(String ciudad) {
        this.ciudad = ciudad;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getComuna() {
        return comuna;
    }

    public void setComuna(String comuna) {
        this.comuna = comuna;
    }
}
