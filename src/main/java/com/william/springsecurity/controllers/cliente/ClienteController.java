package com.william.springsecurity.controllers.cliente;

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

import com.william.springsecurity.repositories.cliente.ClienteRepository;
import com.william.springsecurity.services.ClienteService;
import com.william.springsecurity.domain.cliente.Cliente;;

@RestController
public class ClienteController {
  
    @Autowired
	private ClienteRepository clienteRepository;

	@Autowired
	private ClienteService clienteService;

    @GetMapping("/cliente")
	  public ResponseEntity<List<Cliente>> getAllCliente(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Cliente> cliente = new ArrayList<Cliente>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Cliente> pageTuts;
		  HttpHeaders headers = new HttpHeaders();

	      if (title == null) {
	        pageTuts = clienteRepository.findAll(paging);
			headers.add("x-total-count", String.valueOf(clienteService.getTotalCount()) );
  
	      }else {
	    	    List<Cliente> allCustomers = clienteRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Cliente> pageContent = allCustomers.subList(start, end);
				headers.add("x-total-count", String.valueOf(allCustomers.size()) );

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      cliente = pageTuts.getContent();

	      List<Cliente> response = cliente;

	      return new ResponseEntity<>(response, headers, HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/cliente/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Cliente> clienteById(@PathVariable String id) {
	    
	    try {
			Cliente cliente = clienteRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Cliente>(cliente, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "cliente") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Cliente> salvar(@RequestBody Cliente cliente) { //Reebe os dados para salvar
	    try {
	    	Cliente chamado = clienteRepository.save(cliente);
	    	
	    	return new ResponseEntity<Cliente>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/cliente/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> clienteUpdateById(@PathVariable String id, @RequestBody Cliente cliente) {
	    
	    try {
	    	
	    	if(cliente.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Cliente chamado = clienteRepository.saveAndFlush(cliente);
					    	
	    	return new ResponseEntity<Cliente>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/cliente/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> clienteDeleteById(@PathVariable String id) {
	    
	    try {
			clienteRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

	
}
