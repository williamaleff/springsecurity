package com.william.springsecurity.controllers.user;

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

import com.william.springsecurity.domain.user.User;
import com.william.springsecurity.repositories.UserRepository;
import com.william.springsecurity.services.UserService;

@RestController
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;


     @GetMapping("/user")
	  public ResponseEntity<List<User>> getAllUser(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<User> user = new ArrayList<User>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<User> pageTuts;
		  HttpHeaders headers = new HttpHeaders();

	      if (title == null) {
	        pageTuts = userRepository.findAll(paging);
			headers.add("x-total-count", String.valueOf(userService.getTotalCount()) );
  
	      }else {
	    	    List<User> allCustomers = userRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<User> pageContent = allCustomers.subList(start, end);
				headers.add("x-total-count", String.valueOf(allCustomers.size()) );

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      user = pageTuts.getContent();

	      List<User> response = user;

	      return new ResponseEntity<>(response, headers,HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/user/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<User> userById(@PathVariable String id) {
	    
	    try {
			User user = userRepository.findById(id).get();
					    	
	    	return new ResponseEntity<User>(user, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "user") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<User> salvar(@RequestBody User user) { //Reebe os dados para salvar
	    try {
	    	User chamado = userRepository.save(user);
	    	
	    	return new ResponseEntity<User>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/user/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> userUpdateById(@PathVariable String id, @RequestBody User user) {
	    
	    try {
	    	
	    	if(user.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			User chamado = userRepository.saveAndFlush(user);
					    	
	    	return new ResponseEntity<User>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/user/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> userDeleteById(@PathVariable String id) {
	    
	    try {
			userRepository.deleteById(id);
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

}
