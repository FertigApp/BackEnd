package com.fertigApp.backend.model;


import com.fasterxml.jackson.annotation.JsonIgnore;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity// This tells Hibernate to make a table out of this class
@Table(name = "usuario")
public class Usuario implements Serializable {

	@Id
	private String usuario;

	private String correo;

	private String nombre;

	private boolean google;

	private boolean facebook;

	@JsonIgnore
	private String password;

	@JsonIgnore
	@OneToMany(mappedBy = "usuario")
	private List<TareaDeUsuario> tareas;

	@JsonIgnore
	@ManyToMany(cascade = {CascadeType.ALL})
	@JoinTable(
			name = "preferido",
			joinColumns = {@JoinColumn(name="usuario")},
			inverseJoinColumns = {@JoinColumn(name="id_sonido")}
	)
	List<Sonido> sonidos;

	@JsonIgnore
	@OneToMany(mappedBy = "usuarioE")
	private List<Evento> eventos;

	@JsonIgnore
	@OneToMany(mappedBy = "usuarioR")
	private List<Rutina> rutinas;

    @JsonIgnore
    @ManyToMany(cascade = {CascadeType.ALL})
    @JoinTable(
            name = "amigo",
            joinColumns = {@JoinColumn(name = "agregador")},
            inverseJoinColumns = {@JoinColumn(name = "agregado")}
    )
    List<Usuario> agregadores;

    @JsonIgnore
	@ManyToMany(mappedBy = "agregadores")
	private List<Usuario> agregados;

	public Usuario() { }

	public Usuario(String usuario, String correo, String password, String nombre) {
		this.usuario = usuario;
		this.correo = correo;
		this.nombre = nombre;
		this.password = password;
	}

	public void addAmigo(Usuario amigo) {
		if (this.agregados == null) {
			this.agregados = new ArrayList<>();
		}
		this.agregados.add(amigo);
	}

	public String getCorreo() {
		return correo;
	}

	public void setCorreo(String correo) {
		this.correo = correo;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public List<Evento> getEventos() {
		return eventos;
	}

	public void setEventos(List<Evento> eventos) {
		this.eventos = eventos;
	}

	public List<Rutina> getRutinas() {
		return rutinas;
	}

	public void setRutinas(List<Rutina> rutinas) {
		this.rutinas = rutinas;
	}

	public boolean isGoogle() {
		return google;
	}

	public void setGoogle(boolean google) {
		this.google = google;
	}

	public boolean isFacebook() {
		return facebook;
	}

	public void setFacebook(boolean facebook) {
		this.facebook = facebook;
	}

	public List<TareaDeUsuario> getTareas() {
		return tareas;
	}

	public void setTareas(List<TareaDeUsuario> tareas) {
		this.tareas = tareas;
	}

	public List<Sonido> getSonidos() {
		return sonidos;
	}

	public void setSonidos(List<Sonido> sonidos) {
		this.sonidos = sonidos;
	}

	public List<Usuario> getAgregadores() {
		return agregadores;
	}

	public void setAgregadores(List<Usuario> amigos) {
		this.agregadores = amigos;
	}

	public List<Usuario> getAgregados() {
		return agregados;
	}

	public void setAgregados(List<Usuario> agregados) {
		this.agregados = agregados;
	}
}
