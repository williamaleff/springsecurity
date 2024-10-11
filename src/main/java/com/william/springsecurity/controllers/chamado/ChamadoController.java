package com.william.springsecurity.controllers.chamado;

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

import com.william.springsecurity.repositories.chamado.ChamadoRepository;
import com.william.springsecurity.domain.chamado.Chamado;

@RestController
public class ChamadoController {
    
    @Autowired
	private ChamadoRepository chamadoRepository;

    @GetMapping("/chamado")
	  public ResponseEntity<List<Chamado>> getAllChamado(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Chamado> chamado = new ArrayList<Chamado>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Chamado> pageTuts;
	      if (title == null) {
	        pageTuts = chamadoRepository.findAll(paging);
	      }else {
	    	    List<Chamado> allCustomers = chamadoRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Chamado> pageContent = allCustomers.subList(start, end);

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      chamado = pageTuts.getContent();

	      List<Chamado> response = chamado;

	      return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/chamado/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Chamado> chamadoById(@PathVariable String id) {
	    
	    try {
			Chamado chamado = chamadoRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Chamado>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "chamado") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Chamado> salvar(@RequestBody Chamado chamado) { //Reebe os dados para salvar
	    try {
	    	Chamado suporte = chamadoRepository.save(chamado);
	    	
	    	return new ResponseEntity<Chamado>(suporte, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/chamado/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> chamadoUpdateById(@PathVariable String id, @RequestBody Chamado chamado) {
	    
	    try {
	    	
	    	if(chamado.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Chamado suporte = chamadoRepository.saveAndFlush(chamado);
					    	
	    	return new ResponseEntity<Chamado>(suporte, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/chamado/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> chamadoDeleteById(@PathVariable String id) {
	    
	    try {
			chamadoRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

	
}
