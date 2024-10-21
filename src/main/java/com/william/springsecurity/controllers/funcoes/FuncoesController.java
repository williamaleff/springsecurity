package com.william.springsecurity.controllers.funcoes;

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

import com.william.springsecurity.repositories.funcoes.FuncoesRepository;
import com.william.springsecurity.services.FuncoesService;
import com.william.springsecurity.domain.funcoes.Funcoes;;

@RestController
public class FuncoesController {

    @Autowired
	private FuncoesRepository funcoesRepository;

	@Autowired
	private FuncoesService funcoesService;

    @GetMapping("/funcoes")
	  public ResponseEntity<List<Funcoes>> getAllFuncoes(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Funcoes> funcoes = new ArrayList<Funcoes>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Funcoes> pageTuts;
		  HttpHeaders headers = new HttpHeaders();

	      if (title == null) {
	        pageTuts = funcoesRepository.findAll(paging);
			headers.add("x-total-count", String.valueOf(funcoesService.getTotalCount()) );
  
	      }else {
	    	    List<Funcoes> allCustomers = funcoesRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Funcoes> pageContent = allCustomers.subList(start, end);

				headers.add("x-total-count", String.valueOf(allCustomers.size()) );

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());
	      }
	      funcoes = pageTuts.getContent();

	      List<Funcoes> response = funcoes;

	      return new ResponseEntity<>(response, headers, HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/funcoes/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Funcoes> funcoesById(@PathVariable String id) {
	    
	    try {
			Funcoes funcoes = funcoesRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Funcoes>(funcoes, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "funcoes") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Funcoes> salvar(@RequestBody Funcoes funcoes) { //Reebe os dados para salvar
	    try {
	    	Funcoes chamado = funcoesRepository.save(funcoes);
	    	
	    	return new ResponseEntity<Funcoes>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	     
	    }
	    
	    @RequestMapping(value = "/funcoes/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> funcoesUpdateById(@PathVariable String id, @RequestBody Funcoes funcoes) {
	    
	    try {
	    	
	    	if(funcoes.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Funcoes chamado = funcoesRepository.saveAndFlush(funcoes);
					    	
	    	return new ResponseEntity<Funcoes>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/funcoes/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> funcoesDeleteById(@PathVariable String id) {
	    
	    try {
			funcoesRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

	
}
