package com.fertigapp.backend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fertigApp.backend.BackendApplication;
import com.fertigApp.backend.model.Rutina;
import com.fertigApp.backend.model.Usuario;
import com.fertigApp.backend.requestModels.LoginRequest;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = BackendApplication.class)
@AutoConfigureMockMvc
public class RutinaControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UsuarioService usuarioService;

    @Autowired
    private RutinaService rutinaService;

    private final Jackson2ObjectMapperBuilder mapperBuilder = new Jackson2ObjectMapperBuilder();

    private final ObjectMapper objectMapper = mapperBuilder.build();

    private final Date fechaIncio = new Date();
    private final Date fechaFin = new Date();

    public Usuario setUpUsuario() {
        Usuario user = new Usuario();

        user.setUsuario("test_user");
        user.setCorreo("test@email.com");
        user.setNombre("Test User");
        user.setPassword(passwordEncoder.encode("testing"));
        user.setRutinas(new ArrayList<>());

        this.usuarioService.save(user);
        return user;
    }

    public Rutina setUpRutina(Usuario user) {
        Rutina routine = new Rutina();

        routine.setUsuario(setUpUsuario());
        routine.setNombre("test_routine");
        routine.setDescripcion("test_routine_description");
        routine.setPrioridad(2);
        routine.setEtiqueta("test_routine_tag");
        routine.setEstimacion(90);
        routine.setFechaInicio(this.fechaIncio);
        routine.setFechaFin(this.fechaFin);
        routine.setRecurrencia("codification");
        routine.setRecordatorio(60);

        return rutinaService.save(routine);
    }

    public String getToken(Usuario user) throws Exception {
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
    public void contextLoads(){
        assertTrue(true);
    }


//    @Test
//    public void getAllRutinasByUsuario() throws Exception {
//        String uri = "/routines/getRoutines";
//        Usuario user;
//        List<Rutina> rutinas = new ArrayList<>();
//
//        user = (usuarioService.findById("test_user").isEmpty()) ? setUpUsuario() : usuarioService.findById("test_user").get();
//        for(int i=0; i<5; i++)
//            if (usuarioService.findById("test_user").get().getRutinas() == null || usuarioService.findById("test_user").get().getRutinas().size() < i)
//                rutinas.add(setUpRutina(user));
//            else
//                rutinas.add(user.getRutinas().get(i));
//        String token = getToken(user);
//        ResultActions resultActions = this.mockMvc.perform(get(uri).header("Authorization", "Bearer " + token));
//        assertThat(resultActions.andExpect(status().isOk()));
//        MvcResult mvcResult = resultActions.andReturn();
//        String response = mvcResult.getResponse().getContentAsString();
//        CollectionType javaList = objectMapper.getTypeFactory().constructCollectionType(List.class, Rutina.class);
//        List<Rutina> rutinasObtained = objectMapper.readValue(response, javaList);
//        assertNotNull(rutinasObtained);
//        assertEquals(rutinasObtained.get(0),rutinas.get(0));
//        assertEquals(rutinasObtained.get(1),rutinas.get(1));
//        assertEquals(rutinasObtained.get(2),rutinas.get(2));
//        assertEquals(rutinasObtained.get(3),rutinas.get(3));
//        assertEquals(rutinasObtained.get(4),rutinas.get(4));
//    }

    @Test
    public void getRutina() throws Exception {
        String uri = "/routines/getRoutine";
        Usuario user;
        Rutina rutina;
        user = (usuarioService.findById("test_user").isEmpty()) ? setUpUsuario() : usuarioService.findById("test_user").get();
        rutina = setUpRutina(user);

        String token = getToken(user);
        ResultActions resultActions = this.mockMvc.perform(get(uri+"/"+rutina.getId()).header("Authorization", "Bearer " + token));
        assertThat(resultActions.andExpect(status().isOk()));
        MvcResult mvcResult = resultActions.andReturn();
        String response = mvcResult.getResponse().getContentAsString();
        Rutina rutinaObtained = objectMapper.readValue(response, Rutina.class);
        assertEquals(rutinaObtained.getNombre(), rutina.getNombre());
        assertEquals(rutinaObtained.getDescripcion(), rutina.getDescripcion());
        assertEquals(rutinaObtained.getPrioridad(),rutina.getPrioridad());
        assertEquals(rutinaObtained.getEtiqueta(), rutina.getEtiqueta());
        assertEquals(rutinaObtained.getEstimacion(), rutina.getEstimacion());
        assertTrue(rutinaObtained.getFechaInicio().compareTo(rutina.getFechaInicio()) < 10);
        assertTrue(rutinaObtained.getFechaFin().compareTo(rutina.getFechaFin()) < 10);
        assertEquals(rutinaObtained.getRecurrencia(), rutina.getRecurrencia());
        assertEquals(rutinaObtained.getRecordatorio(), rutina.getRecordatorio());

        rutinaService.deleteById(rutina.getId());
        usuarioService.deleteById("test_user");
    }
}
