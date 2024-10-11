package com.william.springsecurity.controllers.status;

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

import com.william.springsecurity.repositories.status.StatusRepository;
import com.william.springsecurity.domain.status.Status;;

@RestController
public class StatusController {
    @Autowired
	private StatusRepository statusRepository;

    @GetMapping("/status")
	  public ResponseEntity<List<Status>> getAllStatus(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Status> status = new ArrayList<Status>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Status> pageTuts;
	      if (title == null) {
	        pageTuts = statusRepository.findAll(paging);
	      }else {
	    	    List<Status> allCustomers = statusRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Status> pageContent = allCustomers.subList(start, end);

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      status = pageTuts.getContent();

	      List<Status> response = status;

	      return new ResponseEntity<>(response, HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/status/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Status> statusById(@PathVariable String id) {
	    
	    try {
			Status status = statusRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Status>(status, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "status") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Status> salvar(@RequestBody Status status) { //Reebe os dados para salvar
	    try {
	    	Status chamado = statusRepository.save(status);
	    	
	    	return new ResponseEntity<Status>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	     
	    }
	    
	    @RequestMapping(value = "/status/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> statusUpdateById(@PathVariable String id, @RequestBody Status status) {
	    
	    try {
	    	
	    	if(status.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Status chamado = statusRepository.saveAndFlush(status);
					    	
	    	return new ResponseEntity<Status>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/status/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> statusDeleteById(@PathVariable String id) {
	    
	    try {
			statusRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

	
}
