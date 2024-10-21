package com.william.springsecurity.controllers.historico;

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

import com.william.springsecurity.repositories.historico.HistoricoRepository;
import com.william.springsecurity.services.HistoricoService;
import com.william.springsecurity.domain.historico.Historico;;

@RestController
public class HistoricoController {
    @Autowired
	private HistoricoRepository historicoRepository;

	@Autowired
	private HistoricoService historicoService;

    @GetMapping("/historico")
	  public ResponseEntity<List<Historico>> getAllHistorico(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Historico> historico = new ArrayList<Historico>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Historico> pageTuts;
		  HttpHeaders headers = new HttpHeaders();

	      if (title == null) {
	        pageTuts = historicoRepository.findAll(paging);
			headers.add("x-total-count", String.valueOf(historicoService.getTotalCount()) );
  
	      }else {
	    	    List<Historico> allCustomers = historicoRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Historico> pageContent = allCustomers.subList(start, end);
				headers.add("x-total-count", String.valueOf(allCustomers.size()) );

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      historico = pageTuts.getContent();

	      List<Historico> response = historico;

	      return new ResponseEntity<>(response, headers, HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/historico/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Historico> historicoById(@PathVariable String id) {
	    
	    try {
			Historico historico = historicoRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Historico>(historico, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "historico") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Historico> salvar(@RequestBody Historico historico) { //Reebe os dados para salvar
	    try {
	    	Historico chamado = historicoRepository.save(historico);
	    	
	    	return new ResponseEntity<Historico>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	     
	    }
	    
	    @RequestMapping(value = "/historico/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> historicoUpdateById(@PathVariable String id, @RequestBody Historico historico) {
	    
	    try {
	    	
	    	if(historico.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Historico chamado = historicoRepository.saveAndFlush(historico);
					    	
	    	return new ResponseEntity<Historico>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/historico/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> historicoDeleteById(@PathVariable String id) {
	    
	    try {
			historicoRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	
}
