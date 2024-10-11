package com.william.springsecurity.controllers.prioridade;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

import com.william.springsecurity.repositories.prioridade.PrioridadeRepository;
import com.william.springsecurity.domain.prioridade.Prioridade;;

@RestController
public class PrioridadeController {
    @Autowired
	private PrioridadeRepository prioridadeRepository;

    @GetMapping("/prioridade")
	  public ResponseEntity<List<Prioridade>> getAllPrioridade(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Prioridade> prioridade = new ArrayList<Prioridade>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Prioridade> pageTuts;
	      if (title == null) {
	        pageTuts = prioridadeRepository.findAll(paging);
	      }else {
	    	    List<Prioridade> allCustomers = prioridadeRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Prioridade> pageContent = allCustomers.subList(start, end);

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      prioridade = pageTuts.getContent();

	      List<Prioridade> response = prioridade;

	      return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/prioridade/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Prioridade> prioridadeById(@PathVariable String id) {
	    
	    try {
			Prioridade prioridade = prioridadeRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Prioridade>(prioridade, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "prioridade") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Prioridade> salvar(@RequestBody Prioridade prioridade) { //Reebe os dados para salvar
	    try {
	    	Prioridade chamado = prioridadeRepository.save(prioridade);
	    	
	    	return new ResponseEntity<Prioridade>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	     
	    }
	    
	    @RequestMapping(value = "/prioridade/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> prioridadeUpdateById(@PathVariable String id, @RequestBody Prioridade prioridade) {
	    
	    try {
	    	
	    	if(prioridade.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Prioridade chamado = prioridadeRepository.saveAndFlush(prioridade);
					    	
	    	return new ResponseEntity<Prioridade>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/prioridade/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> prioridadeDeleteById(@PathVariable String id) {
	    
	    try {
			prioridadeRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

	
}
