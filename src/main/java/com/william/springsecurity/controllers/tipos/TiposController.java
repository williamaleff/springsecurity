package com.william.springsecurity.controllers.tipos;

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

import com.william.springsecurity.domain.tipos.Tipos;
import com.william.springsecurity.repositories.tipos.TiposRepository;

@RestController
public class TiposController {
    
    @Autowired
	private TiposRepository tiposRepository;
	
    @GetMapping("/tipos")
				  public ResponseEntity<List<Tipos>> getAllTipos(
				        @RequestParam(required = false, name="nome_like") String title,
				        @RequestParam(defaultValue = "1", name="_page") int page,
				        @RequestParam(defaultValue = "3", name="_limit") int size
				      ) {

				    try {
				      List<Tipos> tipos = new ArrayList<Tipos>();
				      Pageable paging = PageRequest.of((page-1), size);
				      
				      Page<Tipos> pageTuts;
				      if (title == null)
				        pageTuts = tiposRepository.findAll(paging);
				      else {
				    	  
				    	    List<Tipos> allCustomers = tiposRepository.findByNameContaining(title);
						    int start = (int) paging.getOffset();
						    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

						    List<Tipos> pageContent = allCustomers.subList(start, end);

				            pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());
				      }
				      tipos = pageTuts.getContent();

				      List<Tipos> response = tipos;
				      
				      return new ResponseEntity<>(response, HttpStatus.OK);
				    } catch (Exception e) {
				      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				    }
				  }
				 
				    @RequestMapping(value = "/tipos/{id}", method = RequestMethod.GET)
				    @ResponseBody
				    public  ResponseEntity<Tipos> tiposById(@PathVariable String id) {
				    
				    try {
						Tipos tipos = tiposRepository.findById(Long.parseLong(id)).get();
								    	
				    	return new ResponseEntity<Tipos>(tipos, HttpStatus.OK);
				    } catch (Exception e) {
					      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				    
				    }
				    
				    @PostMapping(value = "tipos") //mapeia a url
				    @ResponseBody //descrição da resposta
				    public ResponseEntity<Tipos> salvarTipos(@RequestBody Tipos tipos) { //Reebe os dados para salvar
				    try {
				    	Tipos chamado = tiposRepository.save(tipos);
				    	
				    	return new ResponseEntity<Tipos>(chamado, HttpStatus.CREATED);
				    } catch (Exception e) {
					      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				    
				    }
				    
				    @RequestMapping(value = "/tipos/{id}", method = RequestMethod.PUT)
				    @ResponseBody
				    public  ResponseEntity<?> tiposUpdateById(@PathVariable String id, @RequestBody Tipos tipos) {
				    
				    try {
				    	
				    	if(tipos.getId() == null) {
				    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
				    	}
				    	
						Tipos chamado = tiposRepository.saveAndFlush(tipos);
								    	
				    	return new ResponseEntity<Tipos>(chamado, HttpStatus.OK);
				    } catch (Exception e) {
					      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				    
				    }
				    
				    @RequestMapping(value = "/tipos/{id}", method = RequestMethod.DELETE)
				    @ResponseBody
				    public  ResponseEntity<String> tiposDeleteById(@PathVariable String id) {
				    
				    try {
						tiposRepository.deleteById(Long.parseLong(id));
								    	
				    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
				    } catch (Exception e) {
					      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
					}
				    
				    }
 

}
