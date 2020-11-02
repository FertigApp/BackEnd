package com.fertigapp.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fertigApp.backend.BackendApplication;
import com.fertigApp.backend.model.Usuario;
import com.fertigApp.backend.requestModels.LoginRequest;
import com.fertigApp.backend.services.CompletadaService;
import com.fertigApp.backend.services.RutinaService;
import com.fertigApp.backend.services.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.json.JacksonJsonParser;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = BackendApplication.class)
@AutoConfigureMockMvc
class RutinaControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private CompletadaService completadaService;

    @Autowired
    private RutinaService rutinaService;

    private final Jackson2ObjectMapperBuilder mapperBuilder = new Jackson2ObjectMapperBuilder();

    private final ObjectMapper objectMapper = mapperBuilder.build();

    Usuario setUpUsuario() {
        Usuario user = new Usuario();

        user.setUsuario("test_user");
        user.setCorreo("test@email.com");
        user.setNombre("Test User");
        user.setPassword(passwordEncoder.encode("testing"));
        user.setRutinas(new ArrayList<>());

        this.usuarioService.save(user);
        return user;
    }

//    Rutina setUpRutina(Usuario user) {
//        Rutina routine = new Rutina();
//
//        routine.setUsuario(user);
//        routine.setNombre("test_routine");
//        routine.setDescripcion("test_routine_description");
//        routine.setPrioridad(2);
//        routine.setEtiqueta("test_routine_tag");
//        routine.setDuracion(90);
//        routine.setFechaInicio(new Date());
//        routine.setFechaFin(new Date());
//        routine.setRecurrencia("codification");
//        routine.setRecordatorio(60);
//
////        Completada completada = new Completada();
////        completada.setRutina(routine);
////        completada.setFecha(new Date());
////        this.completadaService.save(completada);
//        ArrayList<Completada> completadas = new ArrayList<>();
////        completadas.add(completada);
//        routine.setCompletadas(completadas);
//
//        return rutinaService.save(routine);
//    }

    String getToken(Usuario user) throws Exception {
        String token = "";

        if (usuarioService.existsById(user.getUsuario())) {
            String uri = "/signin";

            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername(user.getUsuario());
            loginRequest.setPassword("testing");

            ResultActions resultActions = this.mockMvc.perform(post(uri).contentType(MediaType.APPLICATION_JSON_VALUE)
                    .content(objectMapper.writeValueAsString(loginRequest)).accept(MediaType.ALL)).andExpect(status().isOk());

            String response = resultActions.andReturn().getResponse().getContentAsString();
            JacksonJsonParser jsonParser = new JacksonJsonParser();
            token = jsonParser.parseMap(response).get("access_token").toString();
        }

        return token;
    }


    @Test
    void contextLoads(){
        assertTrue(true);
    }


//    @Test
//    @WithMockUser(value = "ADMIN")
//    void getAllRutinas() throws Exception {
//        String uri = "/routines";
//        Usuario user;
//        if (this.usuarioService.findById("test_user").isEmpty())
//            user = setUpUsuario();
//        else user = this.usuarioService.findById("test_user").get();
//        Rutina rutina = setUpRutina(user);
//
//        ResultActions resultActions = this.mockMvc.perform(get(uri)).andExpect(status().isOk());
//        MvcResult mvcResult = resultActions.andReturn();
//        String response = mvcResult.getResponse().getContentAsString();
//        CollectionType javaList = objectMapper.getTypeFactory().constructCollectionType(List.class, Rutina.class);
//        List<Rutina> rutinas = objectMapper.readValue(response, javaList);
//        assertNotNull(rutinas);
//
//        this.rutinaService.deleteById(rutina.getId());
//        this.usuarioService.deleteById(user.getUsuario());
//    }

//    @Test
//    void getAllRutinasByUsuario() throws Exception {
//        String uri = "/routines/getRoutines";
//        Usuario user;
//        if (this.usuarioService.findById("test_user").isEmpty())
//            user = setUpUsuario();
//        else user = this.usuarioService.findById("test_user").get();
//        String token = getToken(user);
//
//        List<Rutina> rutinas = new ArrayList<>();
//        for (int index = 0; index < 5; index++) {
//            rutinas.add(setUpRutina(user));
//        }
//
//        ResultActions resultActions = this.mockMvc.perform(get(uri).header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk());
//        MvcResult mvcResult = resultActions.andReturn();
//        String response = mvcResult.getResponse().getContentAsString();
//        CollectionType javaList = objectMapper.getTypeFactory().constructCollectionType(List.class, RutinaResponse.class);
//        List<RutinaResponse> routines = objectMapper.readValue(response, javaList);
//        assertNotNull(routines);
//        assertEquals(routines.size(), rutinas.size());
//        routines.sort(Comparator.comparingInt(o -> o.rutina.getId()));
//        for (int index = 0; index < routines.size(); index++) {
//            assertEquals(routines.get(index).rutina.getUsuario().getUsuario(), rutinas.get(index).getUsuario().getUsuario());
//            assertEquals(routines.get(index).rutina.getNombre(), rutinas.get(index).getNombre());
//            assertEquals(routines.get(index).rutina.getDescripcion(), rutinas.get(index).getDescripcion());
//        }
//
//        for (Rutina rutina : rutinas) {
//            this.rutinaService.deleteById(rutina.getId());
//        }
//        this.usuarioService.deleteById(user.getUsuario());
//    }

//    @Test
//    void getRutina() throws Exception {
//        String uri = "/routines/getRoutine";
//        Usuario user;
//        if (this.usuarioService.findById("test_user").isEmpty())
//            user = setUpUsuario();
//        else user = this.usuarioService.findById("test_user").get();
//        Rutina rutina = setUpRutina(user);
//        String token = getToken(user);
//
//        ResultActions resultActions = this.mockMvc.perform(get(uri+"/"+rutina.getId()).header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk());
//        MvcResult mvcResult = resultActions.andReturn();
//        String response = mvcResult.getResponse().getContentAsString();
//        Rutina rutinaObtained = objectMapper.readValue(response, Rutina.class);
//        assertEquals(rutinaObtained.getNombre(), rutina.getNombre());
//        assertEquals(rutinaObtained.getDescripcion(), rutina.getDescripcion());
//        assertEquals(rutinaObtained.getPrioridad(),rutina.getPrioridad());
//        assertEquals(rutinaObtained.getEtiqueta(), rutina.getEtiqueta());
//        assertEquals(rutinaObtained.getDuracion(), rutina.getDuracion());
//        assertTrue(rutinaObtained.getFechaInicio().compareTo(rutina.getFechaInicio()) < 10);
//        assertTrue(rutinaObtained.getFechaFin().compareTo(rutina.getFechaFin()) < 10);
//        assertEquals(rutinaObtained.getRecurrencia(), rutina.getRecurrencia());
//        assertEquals(rutinaObtained.getRecordatorio(), rutina.getRecordatorio());
//
//        resultActions = this.mockMvc.perform(get(uri + "/" + (rutina.getId() + 1)).header("Authorization", "Bearer " + token))
//                .andExpect(status().isOk());
//        mvcResult = resultActions.andReturn();
//        response = mvcResult.getResponse().getContentAsString();
//        assertTrue(response.isEmpty());
//
//        rutinaService.deleteById(rutina.getId());
//        usuarioService.deleteById("test_user");
//    }

//    @Test
//    void replaceRutina() throws Exception {
//        String uri = "/routines/updateRoutine/";
//        Usuario user;
//        if (this.usuarioService.findById("test_user").isEmpty())
//            user = setUpUsuario();
//        else user = this.usuarioService.findById("test_user").get();
//        Rutina rutina = setUpRutina(user);
//        String token = getToken(user);
//
//        RequestRutina requestRutina = new RequestRutina();
//        requestRutina.setUsuarioR(user);
//        requestRutina.setNombre(rutina.getNombre() + " v2");
//        requestRutina.setDescripcion(rutina.getDescripcion());
//        requestRutina.setPrioridad(rutina.getPrioridad());
//        requestRutina.setEtiqueta(rutina.getEtiqueta());
//        requestRutina.setDuracion(rutina.getDuracion());
//        requestRutina.setFechaInicio(requestRutina.getFechaInicio());
//        requestRutina.setFechaFin(rutina.getFechaFin());
//        requestRutina.setRecurrencia(rutina.getRecurrencia());
//        requestRutina.setRecordatorio(rutina.getRecordatorio());
//        requestRutina.setCompletadas(rutina.getCompletadas());
//
//        ResultActions resultActions = this.mockMvc.perform(put(uri + rutina.getId()).header("Authorization", "Bearer " + token)
//            .contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(requestRutina)))
//                .andExpect(status().isOk());
//        MvcResult mvcResult = resultActions.andReturn();
//        String response = mvcResult.getResponse().getContentAsString();
//        Rutina rutinaObtained = this.objectMapper.readValue(response, Rutina.class);
//        assertNotNull(rutinaObtained);
//        assertEquals(rutinaObtained.getUsuario().getUsuario(), requestRutina.getUsuarioR().getUsuario());
//        assertEquals(rutinaObtained.getNombre(), requestRutina.getNombre());
//        assertEquals(rutinaObtained.getDescripcion(), requestRutina.getDescripcion());
//
//        this.mockMvc.perform(put(uri + (rutina.getId() + 1)).header("Authorization", "Bearer " + token)
//            .contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(requestRutina)))
//                .andExpect(status().isBadRequest());
//
//        this.rutinaService.deleteById(rutina.getId());
//        this.usuarioService.deleteById(user.getUsuario());
//    }

//    @Test
//    void addNewRutina() throws Exception {
//        String uri = "/routines/addRoutine";
//        Usuario user;
//        if (this.usuarioService.findById("test_user").isEmpty())
//            user = setUpUsuario();
//        else user = this.usuarioService.findById("test_user").get();
//        String token = getToken(user);
//
//        RequestRutina requestRutina = new RequestRutina();
//        requestRutina.setUsuarioR(user);
//        requestRutina.setNombre("test_routine");
//        requestRutina.setDescripcion("test_routine_description");
//        requestRutina.setPrioridad(2);
//        requestRutina.setEtiqueta("test_routine_tag");
//        requestRutina.setDuracion(90);
//        requestRutina.setFechaInicio(new Date());
//        requestRutina.setFechaFin(new Date());
//        requestRutina.setRecurrencia("codification");
//        requestRutina.setRecordatorio(60);
//        requestRutina.setCompletadas(new ArrayList<>());
//
//        this.mockMvc.perform(post(uri).header("Authorization", "Bearer " + token)
//            .contentType(MediaType.APPLICATION_JSON_VALUE).content(objectMapper.writeValueAsString(requestRutina)))
//                .andExpect(status().isCreated());
//
//        List<Rutina> rutinas = (List<Rutina>) this.rutinaService.findAll();
//        assertFalse(rutinas.isEmpty());
//        assertEquals(rutinas.get(0).getUsuario().getUsuario(), requestRutina.getUsuarioR().getUsuario());
//        assertEquals(rutinas.get(0).getNombre(), requestRutina.getNombre());
//        assertEquals(rutinas.get(0).getDescripcion(), requestRutina.getDescripcion());
//
//        for (Rutina rutina : rutinas) {
//            this.rutinaService.deleteById(rutina.getId());
//        }
//        this.usuarioService.deleteById(user.getUsuario());
//    }

//    @Test
//    void deleteRutina() throws Exception {
//        String uri = "/routines/deleteRoutine/";
//        Usuario user;
//        if (this.usuarioService.findById("test_user").isEmpty())
//            user = setUpUsuario();
//        else user = this.usuarioService.findById("test_user").get();
//        Rutina rutina = setUpRutina(user);
//        String token = getToken(user);
//
//        this.mockMvc.perform(delete(uri + rutina.getId()).header("Authorization", "Bearer " + token))
//                .andExpect(status().isAccepted());
//
//        this.mockMvc.perform(delete(uri + rutina.getId() + 1).header("Authorization", "Bearer " + token))
//                .andExpect(status().isBadRequest());
//
//        this.usuarioService.deleteById(user.getUsuario());
//    }

}
