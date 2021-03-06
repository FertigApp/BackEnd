package com.fertigapp.backend.controller;

import com.fertigapp.backend.recurrentstrategy.RutinaRecurrentEntityStrategy;
import com.fertigapp.backend.firebase.NotificationSystem;
import com.fertigapp.backend.model.*;
import com.fertigapp.backend.payload.response.MessageResponse;
import com.fertigapp.backend.payload.response.RecurrenteResponse;
import com.fertigapp.backend.payload.response.RutinaRepeticionesResponse;
import com.fertigapp.backend.requestmodels.RequestRutina;
import com.fertigapp.backend.requestmodels.RequestTarea;
import com.fertigapp.backend.services.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/*
 * Clase responsable de manejar request de tipo GET, POST, PUT y DELETE para
 * la entidad "Rutina".
 * */
@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
public class RutinaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RutinaController.class);

    private static final String RUT_NO_PERTENECE = "La rutina no pertenece al usuario";
    private static final String RUT_NO_ENCONTRADA = "Rutina no encontrada";
    private static final String TAR_NO_ENCONTRADA = "Tarea no encontrada";

    // Repositorio responsable del manejo de la tabla "rutina" en la DB.
    private final RutinaService rutinaService;

    // Repositorio responsable del manejo de la tabla "usuario" en la DB.
    private final UsuarioService usuarioService;

    private final TareaService tareaService;

    private final CompletadaService completadaService;

    private final NotificationSystem notificationSystem;

    public RutinaController(RutinaService rutinaService, UsuarioService usuarioService, TareaService tareaService, CompletadaService completadaService, NotificationSystem notificationSystem) {
        this.rutinaService = rutinaService;
        this.usuarioService = usuarioService;
        this.tareaService = tareaService;
        this.completadaService = completadaService;
        this.notificationSystem = notificationSystem;
    }

    // Método GET para obtener todas las entidades de tipo "Rutina" almacenadas en la DB.
    @GetMapping(path="/routines")
    public @ResponseBody ResponseEntity<List<Rutina>> getAllRutinas() {
        List<Rutina> rutinas = (List<Rutina>) this.rutinaService.findAll();
        return ResponseEntity.ok(rutinas);
    }

    // Método GET para obtener todas las rutinas de un usuario específico.
    @GetMapping(path="/routine/routines")
    public ResponseEntity<List<RecurrenteResponse>> getAllRutinasByUsuario() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Usuario> optionalUsuario = usuarioService.findById(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(null);
        List<Rutina> rutinas = (List<Rutina>) rutinaService.findByUsuario(usuario);
        List<RecurrenteResponse> rutinaResponses = new ArrayList<>();
        for(Rutina rutina : rutinas) {
            rutinaResponses.add(new RecurrenteResponse(rutina, completadaService.findFechaNoCompletadaByRutina(rutina)));
        }
        return ResponseEntity.ok().body(rutinaResponses);
    }

    @GetMapping(path="/routine/checked-routines")
    public ResponseEntity<List<RecurrenteResponse>> getAllCheckeadas(){
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Usuario> optionalUsuario = usuarioService.findById(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        List<Rutina> rutinas = (List<Rutina>) rutinaService.findByUsuario(usuario);
        List<RecurrenteResponse> rutinaResponses = new ArrayList<>();
        for(Rutina rutina : rutinas) {
            RecurrenteResponse response = new RecurrenteResponse(rutina, completadaService.findMaxFechaCompletadaByRutina(rutina));
            rutinaResponses.add(response);
        }
        return ResponseEntity.ok().body(rutinaResponses);
    }

    @GetMapping(path="/routine/routines-and-repetitions")
    public ResponseEntity<List<RutinaRepeticionesResponse>> getAllRutinasRepeticionesByUsuario() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Usuario> optionalUsuario = usuarioService.findById(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        List<Rutina> rutinas = (List<Rutina>) this.rutinaService.findByUsuario(usuario);
        List<RutinaRepeticionesResponse> response = new LinkedList<>();
        for(Rutina rutina : rutinas){
            response.add(new RutinaRepeticionesResponse(
                    rutina,
                    (List<OffsetDateTime>) completadaService.findFechasCompletadasByRutina(rutina)));
        }
        return ResponseEntity.ok().body(response);
    }

    // Método GET para obtener una rutina específica por medio de su ID.
    @GetMapping(path="/routine/{id}")
    public ResponseEntity<Rutina> getRutina(@PathVariable Integer id) {
        Optional<Rutina> optionalRutina = this.rutinaService.findById(id);
        if (optionalRutina.isEmpty()) {
            LOGGER.info("La rutina no existe");
            return ResponseEntity.badRequest().body(null);
        }
        Rutina rutina = optionalRutina.get();
        return ResponseEntity.ok(rutina);
    }

    // Método PUT para modificar un registro en la base de datos.
    @PutMapping(path="/routine/update/{id}")
    public ResponseEntity<Rutina> replaceRutina(@PathVariable Integer id, @RequestBody RequestRutina routine) {
        Object principal =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        Optional<Rutina> optionalRutina = this.rutinaService.findById(id);
        if (optionalRutina.isEmpty()) {
            LOGGER.info(RUT_NO_ENCONTRADA);
            return ResponseEntity.badRequest().body(null);
        }
        Optional<Usuario> optionalUsuario = usuarioService.findByUsuario(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        Rutina rutina = optionalRutina.get();
        if (!rutina.getUsuario().getUsuario().equals(usuario.getUsuario())) {
            LOGGER.info(RUT_NO_PERTENECE);
            return ResponseEntity.badRequest().body(null);
        }
        rutina.setNombre(routine.getNombre());
        rutina.setDescripcion(routine.getDescripcion());
        rutina.setPrioridad(routine.getPrioridad());
        rutina.setEtiqueta(routine.getEtiqueta());
        rutina.setDuracion(routine.getDuracion());
        rutina.setFechaInicio(routine.getFechaInicio());
        rutina.setFechaFin(routine.getFechaFin());
        rutina.setRecurrencia(routine.getRecurrencia());
        rutina.setRecordatorio(routine.getRecordatorio());
        rutina.setFranjaInicio(routine.getFranjaInicio());
        rutina.setFranjaFin(routine.getFranjaFin());
        this.rutinaService.save(rutina);
        LOGGER.info("Rutina actualizada");
        return ResponseEntity.ok().body(rutina);
    }

    // Método POST para añadir un registro en la tabla "rutina" de la DB.
    @PostMapping(path="/routine/add")
    public @ResponseBody ResponseEntity<MessageResponse> addNewRutina(@RequestBody RequestRutina requestRutina) {
        Rutina rutina = new Rutina();
        Object principal =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        Optional<Usuario> optUsuario =usuarioService.findById(userDetails.getUsername());
        rutina.setUsuario(optUsuario.orElse(null));
        rutina.setNombre(requestRutina.getNombre());
        rutina.setDescripcion(requestRutina.getDescripcion());
        rutina.setPrioridad(requestRutina.getPrioridad());
        rutina.setEtiqueta(requestRutina.getEtiqueta());
        rutina.setDuracion(requestRutina.getDuracion());
        rutina.setRecurrencia(requestRutina.getRecurrencia());
        rutina.setRecordatorio(requestRutina.getRecordatorio());
        rutina.setFechaInicio(requestRutina.getFechaInicio());
        rutina.setFechaFin(requestRutina.getFechaFin());
        rutina.setFranjaInicio(requestRutina.getFranjaInicio());
        rutina.setFranjaFin(requestRutina.getFranjaFin());
        Rutina savedRutina =  this.rutinaService.save(rutina);

        RutinaRecurrentEntityStrategy rutinaRecurrentEntityStrategy = new RutinaRecurrentEntityStrategy(rutina);

        Completada completada = new Completada();
        completada.setRutinaC(rutina);
        completada.setFecha(rutinaRecurrentEntityStrategy.findSiguiente(OffsetDateTime.now()));
        completada.setFechaAjustada(null);
        completada.setHecha(false);
        this.completadaService.save(completada);
        if (rutina.getRecordatorio() != null) {
            this.notificationSystem.scheduleRoutineNotification(userDetails.getUsername(), savedRutina.getId());
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Rutina creada"));
    }

    @PostMapping(path = "/routine/{id}/add-subtask")
    public ResponseEntity<MessageResponse> addSubtask(@PathVariable Integer id, @RequestBody RequestTarea requestTarea) {
        Optional<Rutina> optionalRutina = this.rutinaService.findById(id);
        if (optionalRutina.isEmpty()) {
            LOGGER.info(RUT_NO_ENCONTRADA);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_ENCONTRADA));
        }
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Usuario> optionalUsuario = this.usuarioService.findById(userDetails.getUsername());
        Rutina rutina = optionalRutina.get();
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        if (!rutina.getUsuario().getUsuario().equals(usuario.getUsuario())) {
            LOGGER.info(RUT_NO_PERTENECE);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_PERTENECE));
        }
        Tarea subtarea = new Tarea();
        subtarea.setDescripcion(requestTarea.getDescripcion());
        subtarea.setEstimacion(requestTarea.getEstimacion());
        subtarea.setEtiqueta(requestTarea.getEtiqueta());
        subtarea.setFechaFin(requestTarea.getFechaFin());
        subtarea.setHecha(requestTarea.getHecha());
        subtarea.setNivel(2);
        subtarea.setNombre(requestTarea.getNombre());
        subtarea.setPrioridad(requestTarea.getPrioridad());
        subtarea.setRecordatorio(requestTarea.getRecordatorio());
        subtarea.setRutinaT(rutina);
        rutina.addSubtarea(subtarea);
        this.rutinaService.save(rutina);
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Subtarea de rutina creada"));
    }

    @PutMapping(path = "/routine/{id}/update-subtask")
    public ResponseEntity<MessageResponse> updateSubtask(@PathVariable Integer id, @RequestBody RequestTarea requestTarea) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Tarea> optionalTarea = this.tareaService.findById(id);
        if (optionalTarea.isEmpty()) {
            LOGGER.info(TAR_NO_ENCONTRADA);
            return ResponseEntity.badRequest().body(new MessageResponse(TAR_NO_ENCONTRADA));
        }
        Tarea subtask = optionalTarea.get();
        Rutina rutina;
        if (subtask.getNivel() == 2) {
            rutina = subtask.getRutinaT();
        } else {
            rutina = subtask.getPadre().getRutinaT();
        }
        Optional<Usuario> optionalUsuario = this.usuarioService.findById(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        if (!rutina.getUsuario().getUsuario().equals(usuario.getUsuario())) {
            LOGGER.info(RUT_NO_PERTENECE);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_PERTENECE));
        }
        subtask.setNombre(requestTarea.getNombre());
        subtask.setDescripcion(requestTarea.getDescripcion());
        subtask.setPrioridad(requestTarea.getPrioridad());
        subtask.setEstimacion(requestTarea.getEstimacion());
        subtask.setEtiqueta(requestTarea.getEtiqueta());
        subtask.setFechaFin(requestTarea.getFechaFin());
        subtask.setHecha(requestTarea.getHecha());
        subtask.setRecordatorio(requestTarea.getRecordatorio());
        this.tareaService.save(subtask);
        return ResponseEntity.ok(new MessageResponse("Subtarea actualizada"));
    }

    @PatchMapping(path = "/routine/{id}/check-subtask")
    public ResponseEntity<MessageResponse> checkSubtask(@PathVariable Integer id) {
        Optional<Tarea> optionalTarea = this.tareaService.findById(id);
        if (optionalTarea.isEmpty()) {
            LOGGER.info(TAR_NO_ENCONTRADA);
            return ResponseEntity.badRequest().body(new MessageResponse(TAR_NO_ENCONTRADA));
        }
        Rutina rutina;
        Tarea subtask = optionalTarea.get();
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Usuario> optionalUsuario = this.usuarioService.findById(userDetails.getUsername());
        if (subtask.getNivel() == 2) {
            rutina = subtask.getRutinaT();
        } else {
            rutina = subtask.getPadre().getRutinaT();
        }
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        if (!rutina.getUsuario().getUsuario().equals(usuario.getUsuario())) {
            LOGGER.info(RUT_NO_PERTENECE);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_PERTENECE));
        }
        subtask.setHecha(!subtask.getHecha());
        this.tareaService.save(subtask);
        return ResponseEntity.ok(new MessageResponse("Subtarea checkeada"));
    }

    @DeleteMapping(path = "/routine/{id}/delete-subtask")
    public ResponseEntity<MessageResponse> deleteSubtask(@PathVariable Integer id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Tarea> optionalTarea = this.tareaService.findById(id);
        if (optionalTarea.isEmpty()) {
            LOGGER.info(TAR_NO_ENCONTRADA);
            return ResponseEntity.badRequest().body(new MessageResponse(TAR_NO_ENCONTRADA));
        }
        Optional<Usuario> optionalUsuario = this.usuarioService.findById(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        Tarea subtask = optionalTarea.get();
        Rutina rutina;
        if (subtask.getNivel() == 2) {
            rutina = subtask.getRutinaT();
        } else {
            rutina = subtask.getPadre().getRutinaT();
        }
        if (!rutina.getUsuario().getUsuario().equals(usuario.getUsuario())) {
            LOGGER.info(RUT_NO_PERTENECE);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_PERTENECE));
        }
        if (subtask.getNivel() == 2) {
            rutina.deleteSubtarea(subtask);
            subtask.setRutinaT(null);
            this.tareaService.save(subtask);
            this.rutinaService.save(rutina);
        } else {
            Tarea parent = subtask.getPadre();
            parent.deleteSubtarea(subtask);
            subtask.setPadre(null);
            this.tareaService.save(subtask);
            this.tareaService.save(parent);
        }
        this.tareaService.deleteById(subtask.getId());
        return ResponseEntity.ok(new MessageResponse("Subtarea eliminada"));
    }

    //@PutMapping
    @PatchMapping(path="/routine/check/{id}")
    public ResponseEntity<MessageResponse> checkRoutine(@PathVariable Integer id){
        Object principal =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails) principal;
        Optional<Rutina> optionalRutina = this.rutinaService.findById(id);
        if (optionalRutina.isEmpty()) {
            LOGGER.info(RUT_NO_ENCONTRADA);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_ENCONTRADA));
        }
        Optional<Usuario> optionalUsuario = this.usuarioService.findByUsuario(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        Rutina rutina = optionalRutina.get();
        if (!rutina.getUsuario().getUsuario().equals(usuario.getUsuario())) {
            LOGGER.info(RUT_NO_PERTENECE);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_PERTENECE));
        }
        RutinaRecurrentEntityStrategy rutinaRecurrentEntityStrategy = new RutinaRecurrentEntityStrategy(rutina);

        Completada completada = this.completadaService.findTopHechaByRutinaAndHecha(rutina,false);
        if (completada == null) return ResponseEntity.badRequest().body(new MessageResponse("Rutina no se puede checkear"));
        OffsetDateTime anterior = rutinaRecurrentEntityStrategy.findAnterior(OffsetDateTime.now());
        if(anterior != null && anterior.isAfter(completada.getFecha())) completada.setFecha(anterior);
        completada.setHecha(true);
        this.completadaService.save(completada);
        Completada newCompletada = new Completada();
        newCompletada.setRutinaC(rutina);

        newCompletada.setFecha( rutinaRecurrentEntityStrategy.findSiguiente(completada.getFecha().plusMinutes(10)));
        newCompletada.setHecha(false);
        if (rutina.getRecordatorio() != null) {
            this.notificationSystem.cancelScheduledRoutineNotification(rutina.getId());
            this.notificationSystem.scheduleRoutineNotification(userDetails.getUsername(), rutina.getId());
        }
        LOGGER.info("Routine repetition checked");
        this.completadaService.save(newCompletada);
        return ResponseEntity.ok().body(new MessageResponse("Routine repetition checked"));
    }

    @PatchMapping(path="/routine/uncheck/{id}")
    public ResponseEntity<MessageResponse> uncheckRoutine(@PathVariable Integer id){
        Object principal =  SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        UserDetails userDetails = (UserDetails) principal;

        Optional<Rutina> optionalRutina = rutinaService.findById(id);
        Optional<Usuario> optionalUsuario = usuarioService.findByUsuario(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());

        if(optionalRutina.isPresent()){
            if(!optionalRutina.get().getUsuario().getUsuario().equals(usuario.getUsuario())){
                return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_PERTENECE));
            }
            Completada completada = this.completadaService.findTopHechaByRutinaAndHecha(optionalRutina.get(), false);
            completadaService.deleteById(completada.getId());
            completada = completadaService.findMaxCompletada(optionalRutina.get());
            if (completada == null) return ResponseEntity.badRequest().body(new MessageResponse("Rutina no se puede descheckear"));
            completada.setHecha(false);
            this.completadaService.save(completada);
            LOGGER.info("Routine repetition unchecked");
            return ResponseEntity.ok().body(new MessageResponse("Routine repetition unchecked"));
        } else {
            LOGGER.info("Routine not found");
            return ResponseEntity.badRequest().body(new MessageResponse("Routine not found"));
        }
    }

    // Método DELETE para borrar un registro en la tabla "rutina" de la DB.
    @DeleteMapping(path="/routine/delete/{id}")
    public ResponseEntity<MessageResponse> deleteRutina(@PathVariable Integer id) {
        Optional<Rutina> optionalRutina = this.rutinaService.findById(id);
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (optionalRutina.isEmpty()) {
            LOGGER.info(RUT_NO_ENCONTRADA);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_ENCONTRADA));
        }
        Rutina rutina = optionalRutina.get();
        Optional<Usuario> optionalUsuario = this.usuarioService.findById(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());
        if (!optionalRutina.get().getUsuario().getUsuario().equals(usuario.getUsuario())) {
            LOGGER.info(RUT_NO_PERTENECE);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_PERTENECE));
        }
        this.completadaService.deleteAllByRutina(rutina);
        this.rutinaService.deleteById(rutina.getId());
        this.notificationSystem.cancelScheduledRoutineNotification(rutina.getId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(new MessageResponse("Rutina eliminada"));
    }

    @PostMapping(path = "/routine/{id}/copy")
    public ResponseEntity<MessageResponse> copyRoutine(@PathVariable Integer id) {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Optional<Rutina> optionalRutina = this.rutinaService.findById(id);
        if (optionalRutina.isEmpty()) {
            LOGGER.info(RUT_NO_ENCONTRADA);
            return ResponseEntity.badRequest().body(new MessageResponse(RUT_NO_ENCONTRADA));
        }
        Rutina rutina = optionalRutina.get();
        if (rutina.getUsuario().getUsuario().equals(userDetails.getUsername())) {
            LOGGER.info("La rutina ya pertenece al usuario");
            return ResponseEntity.badRequest().body(new MessageResponse("Error: la rutina ya pertenece al usuario"));
        }
        Optional<Usuario> optionalUsuario = this.usuarioService.findById(userDetails.getUsername());
        Usuario usuario = optionalUsuario.orElse(new Usuario());

        Rutina copy = new Rutina();
        copy.setNombre(rutina.getNombre());
        copy.setUsuario(usuario);
        copy.setPrioridad(rutina.getPrioridad());
        copy.setDuracion(rutina.getDuracion());
        copy.setRecurrencia(rutina.getRecurrencia());
        copy.setRecordatorio(rutina.getRecordatorio());
        copy.setDescripcion(rutina.getDescripcion());
        copy.setFechaInicio(rutina.getFechaInicio());
        copy.setFechaFin(rutina.getFechaFin());
        copy.setFranjaInicio(rutina.getFranjaInicio());
        copy.setEtiqueta(rutina.getEtiqueta());
        copy.setFranjaFin(rutina.getFranjaFin());
        Rutina savedRutina =  this.rutinaService.save(copy);

        RutinaRecurrentEntityStrategy rutinaRecurrentEntityStrategy = new RutinaRecurrentEntityStrategy(rutina);

        Completada completada = new Completada();
        completada.setRutinaC(copy);
        completada.setFecha(rutinaRecurrentEntityStrategy.findSiguiente(OffsetDateTime.now()));
        completada.setFechaAjustada(null);
        completada.setHecha(false);
        this.completadaService.save(completada);
        if (copy.getRecordatorio() != null) {
            this.notificationSystem.scheduleRoutineNotification(userDetails.getUsername(), savedRutina.getId());
        }
        return ResponseEntity.ok(new MessageResponse("Rutina creada"));
    }
}
