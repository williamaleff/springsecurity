package com.william.springsecurity.controllers.comentario;

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

import com.william.springsecurity.repositories.comentario.ComentarioRepository;
import com.william.springsecurity.services.ComentarioService;
import com.william.springsecurity.domain.comentario.Comentario;;

@RestController
public class ComentarioController {

    @Autowired
	private ComentarioRepository comentarioRepository;

	@Autowired
	private ComentarioService comentarioService;

    @GetMapping("/comentario")
	  public ResponseEntity<List<Comentario>> getAllComentario(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Comentario> comentario = new ArrayList<Comentario>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Comentario> pageTuts;
		  HttpHeaders headers = new HttpHeaders();

	      if (title == null) {
	        pageTuts = comentarioRepository.findAll(paging);
			headers.add("x-total-count", String.valueOf(comentarioService.getTotalCount()) );
  
	      }else {
	    	    List<Comentario> allCustomers = comentarioRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Comentario> pageContent = allCustomers.subList(start, end);

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      comentario = pageTuts.getContent();

	      List<Comentario> response = comentario;

	      return new ResponseEntity<>(response, headers, HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/comentario/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Comentario> comentarioById(@PathVariable String id) {
	    
	    try {
			Comentario comentario = comentarioRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Comentario>(comentario, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "comentario") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Comentario> salvar(@RequestBody Comentario comentario) { //Reebe os dados para salvar
	    try {
	    	Comentario chamado = comentarioRepository.save(comentario);
	    	
	    	return new ResponseEntity<Comentario>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/comentario/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> comentarioUpdateById(@PathVariable String id, @RequestBody Comentario comentario) {
	    
	    try {
	    	
	    	if(comentario.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Comentario chamado = comentarioRepository.saveAndFlush(comentario);
					    	
	    	return new ResponseEntity<Comentario>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/comentario/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> comentarioDeleteById(@PathVariable String id) {
	    
	    try {
			comentarioRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	
}
