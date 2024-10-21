package com.william.springsecurity.controllers.nivel;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.william.springsecurity.repositories.nivel.NivelRepository;
import com.william.springsecurity.services.NivelService;
import com.william.springsecurity.domain.nivel.Nivel;

@RestController
public class NivelController {
    @Autowired
	private NivelRepository nivelRepository;

	@Autowired
	private NivelService nivelService;

    @GetMapping("/nivel")
	  public ResponseEntity<List<Nivel>> getAllNivel(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Nivel> nivel = new ArrayList<Nivel>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Nivel> pageTuts;
		  HttpHeaders headers = new HttpHeaders();

	      if (title == null) {
	        pageTuts = nivelRepository.findAll(paging);
			headers.add("x-total-count", String.valueOf(nivelService.getTotalCount()) );
  
	      }else {
	    	    List<Nivel> allCustomers = nivelRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Nivel> pageContent = allCustomers.subList(start, end);
				headers.add("x-total-count", String.valueOf(allCustomers.size()) );

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      nivel = pageTuts.getContent();

	      List<Nivel> response = nivel;

	      return new ResponseEntity<>(response, headers, HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/nivel/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Nivel> nivelById(@PathVariable String id) {
	    
	    try {
			Nivel nivel = nivelRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Nivel>(nivel, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "nivel") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Nivel> salvar(@RequestBody Nivel nivel) { //Reebe os dados para salvar
	    try {
	    	Nivel chamado = nivelRepository.save(nivel);
	    	
	    	return new ResponseEntity<Nivel>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	     
	    }
	    
	    @RequestMapping(value = "/nivel/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> nivelUpdateById(@PathVariable String id, @RequestBody Nivel nivel) {
	    
	    try {
	    	
	    	if(nivel.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Nivel chamado = nivelRepository.saveAndFlush(nivel);
					    	
	    	return new ResponseEntity<Nivel>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/nivel/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> nivelDeleteById(@PathVariable String id) {
	    
	    try {
			nivelRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

	
}
