package com.william.springsecurity.controllers.agente;

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

import com.william.springsecurity.repositories.agente.AgenteRepository;
import com.william.springsecurity.services.AgenteService;
import com.william.springsecurity.domain.agente.Agente;;

@RestController
public class AgenteController {
  
    @Autowired
	private AgenteRepository agenteRepository;

	@Autowired
	private AgenteService agenteService;

    @GetMapping("/agente")
	  public ResponseEntity<List<Agente>> getAllAgente(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Agente> agente = new ArrayList<Agente>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Agente> pageTuts;
		  HttpHeaders headers = new HttpHeaders();

	      if (title == null) {
	        pageTuts = agenteRepository.findAll(paging);
			headers.add("x-total-count", String.valueOf(agenteService.getTotalCount()) );
  
	      }else {
	    	    List<Agente> allCustomers = agenteRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Agente> pageContent = allCustomers.subList(start, end);
				headers.add("x-total-count", String.valueOf(allCustomers.size()) );

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      agente = pageTuts.getContent();

	      List<Agente> response = agente;

	      return new ResponseEntity<>(response, headers,HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/agente/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Agente> agenteById(@PathVariable String id) {
	    
	    try {
			Agente agente = agenteRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Agente>(agente, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "agente") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Agente> salvar(@RequestBody Agente agente) { //Reebe os dados para salvar
	    try {
	    	Agente chamado = agenteRepository.save(agente);
	    	
	    	return new ResponseEntity<Agente>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/agente/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> agenteUpdateById(@PathVariable String id, @RequestBody Agente agente) {
	    
	    try {
	    	
	    	if(agente.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Agente chamado = agenteRepository.saveAndFlush(agente);
					    	
	    	return new ResponseEntity<Agente>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/agente/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> agenteDeleteById(@PathVariable String id) {
	    
	    try {
			agenteRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

	
}
