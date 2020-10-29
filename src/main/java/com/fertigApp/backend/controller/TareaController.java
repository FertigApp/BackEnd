package com.fertigApp.backend.controller;

import com.fertigApp.backend.auth.services.UserDetailsImpl;
import com.fertigApp.backend.model.Completada;
import com.fertigApp.backend.model.Tarea;
import com.fertigApp.backend.model.Usuario;
import com.fertigApp.backend.payload.response.MessageResponse;
import com.fertigApp.backend.requestModels.RequestTarea;
import com.fertigApp.backend.services.TareaService;
import com.fertigApp.backend.services.UsuarioService;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * Clase responsable de manejar request de tipo GET, POST, PUT y DELETE para
 * la entidad "Tarea".
 * */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class TareaController {

    private static final org.slf4j.Logger LOGGER= LoggerFactory.getLogger(Completada.class);

    // Repositorio responsable del manejo de la tabla "tarea" en la DB.
    private final TareaService tareaService;

    // Repositorio responsable del manejo de la tabla "usuario" en la DB.
    private final UsuarioService usuarioService;

    public TareaController(TareaService tareaService, UsuarioService usuarioService) {
        this.tareaService = tareaService;
        this.usuarioService = usuarioService;
    }

    // Método GET para obtener todas las entidades de tipo "Tarea" almacenadas en la DB.
    @GetMapping(path="/tasks")
    public @ResponseBody Iterable<Tarea> getAllTareas() {
        return this.tareaService.findAll();
    }


    // Método GET para obtener todas las tareas de un usuario específico.
    @GetMapping(path="/tasks/getTasks")
    public Iterable<Tarea> getAllTareasByUsuario() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        Optional<Usuario> optUsuario = usuarioService.findById(userDetails.getUsername());
        return optUsuario.map(tareaService::findByUsuario).orElse(null);

    }

    // Método GET para obtener una entidad de tipo "tarea" por medio de su ID.
    @GetMapping(path="/tasks/getTask/{id}")
    public Tarea getTarea(@PathVariable Integer id) {
        Optional<Tarea> optTarea = this.tareaService.findById(id);
        return (optTarea.orElse(null));
    }

    @PutMapping(path="/tasks/updateTask/{id}")
    public ResponseEntity<Tarea> replaceTarea(@PathVariable Integer id, @RequestBody RequestTarea task) {
        Object principal =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Logger.getGlobal().log(Level.INFO,principal.toString());
        UserDetails userDetails = (UserDetails) principal;

        Optional<Tarea> optionalTarea = tareaService.findById(id);
        Optional<Usuario> optionalUsuario = usuarioService.findByUsuario(userDetails.getUsername());
        if(optionalTarea.isPresent() && optionalUsuario.isPresent()){
            Tarea tarea = optionalTarea.get();
            tarea.setUsuario(optionalUsuario.get());
            tarea.setNombre(task.getNombre());
            tarea.setDescripcion(task.getDescripcion());
            tarea.setPrioridad(task.getPrioridad());
            tarea.setEtiqueta(task.getEtiqueta());
            tarea.setEstimacion(task.getEstimacion());
            tarea.setFechaInicio(task.getFechaInicio());
            tarea.setFechaFin(task.getFechaFin());
            tarea.setNivel(task.getNivel());
            tarea.setHecha(task.getHecha());
            this.tareaService.save(tarea);
            LOGGER.info("Task updated");
            return ResponseEntity.ok().body(tarea);
        } else {
            LOGGER.info("Task not found");
            return ResponseEntity.badRequest().body(null);
        }
    }

    @PatchMapping(path="/tasks/checkTask/{id}")
    public ResponseEntity<MessageResponse> checkTarea(@PathVariable Integer id) {
        Optional<Tarea> optionalTarea = tareaService.findById(id);
        if(optionalTarea.isPresent()){
            Tarea tarea = optionalTarea.get();
            UserDetailsImpl principal = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (tarea.getUsuarioT().getCorreo().equals(principal.getEmail())){
                tarea.setHecha(!tarea.getHecha());
                this.tareaService.save(tarea);
                return ResponseEntity.ok().body(null);
            }
            return ResponseEntity.badRequest().body(new MessageResponse("Error:Tarea pertieneciente a otro usuario"));
        } else {
            return ResponseEntity.badRequest().body(new MessageResponse("Error:Tarea inexistente"));
        }
    }

    // Método POST para agregar un registro en la tabla "tarea" de la DB.
    @PostMapping(path="/tasks/addTask")
    public @ResponseBody ResponseEntity<Void> addNewTarea(@RequestBody RequestTarea requestTarea) {
        Tarea tarea= new Tarea();
        Object principal =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Logger.getGlobal().log(Level.INFO,principal.toString());
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> optUsuario = usuarioService.findById(userDetails.getUsername());
        tarea.setUsuario(optUsuario.orElse(null));
        tarea.setDescripcion(requestTarea.getDescripcion());
        tarea.setEstimacion(requestTarea.getEstimacion());
        tarea.setEtiqueta(requestTarea.getEtiqueta());
        tarea.setFechaFin(requestTarea.getFechaFin());
        tarea.setFechaInicio(requestTarea.getFechaInicio());
        tarea.setHecha(requestTarea.getHecha());
        tarea.setNivel(requestTarea.getNivel());
        tarea.setNombre(requestTarea.getNombre());
        tarea.setPrioridad(requestTarea.getPrioridad());
        tarea.setRecordatorio(requestTarea.getRecordatorio());
        this.tareaService.save(tarea);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    // Método DELETE para borrar un registro de la tabla "tarea" en la DB.
    @DeleteMapping(path="/tasks/deleteTask/{id}")
    //@RequestParam
    public ResponseEntity<Void> deleteTarea(@PathVariable Integer id) {
        if (!this.tareaService.findById(id).isPresent())
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        this.tareaService.deleteById(id);
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
