package com.william.springsecurity.controllers.pessoas;

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

import com.william.springsecurity.domain.pessoas.Pessoas;
import com.william.springsecurity.repositories.pessoas.PessoasRepository;

@RestController
public class PessoasController {
    
	@Autowired
	private PessoasRepository pessoasRepository;

      @GetMapping("/pessoas")
			  public ResponseEntity<List<Pessoas>> getAllPessoas(
			        @RequestParam(required = false, name="nomeCompleto_like") String title,
			        @RequestParam(defaultValue = "1", name="_page") int page,
			        @RequestParam(defaultValue = "3", name="_limit") int size
			      ) {

			    try {
			      List<Pessoas> pessoas = new ArrayList<Pessoas>();
			      Pageable paging = PageRequest.of((page-1), size);
			      
			      Page<Pessoas> pageTuts;
			      if (title == null)
			        pageTuts = pessoasRepository.findAll(paging);
			      else {
  			    	    
			    	    List<Pessoas> allCustomers = pessoasRepository.findByNameContaining(title);
					    int start = (int) paging.getOffset();
					    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

					    List<Pessoas> pageContent = allCustomers.subList(start, end);

			        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());
			      }
			      pessoas = pageTuts.getContent();

			      List<Pessoas> response = pessoas;
			      
			      return new ResponseEntity<>(response, HttpStatus.OK);
			    } catch (Exception e) {
			      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			    }
			  }
			 
			    @RequestMapping(value = "/pessoas/{id}", method = RequestMethod.GET)
			    @ResponseBody
			    public  ResponseEntity<Pessoas> pessoasById(@PathVariable String id) {
			    
			    try {
					Pessoas pessoas = pessoasRepository.findById(Long.parseLong(id)).get();
							    	
			    	return new ResponseEntity<Pessoas>(pessoas, HttpStatus.OK);
			    } catch (Exception e) {
				      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			    
			    }
			    
			    @PostMapping(value = "pessoas") //mapeia a url
			    @ResponseBody //descrição da resposta
			    public ResponseEntity<Pessoas> salvarPessoa(@RequestBody Pessoas pessoa) { //Reebe os dados para salvar
			    try {
			    	Pessoas chamado = pessoasRepository.save(pessoa);
			    	
			    	return new ResponseEntity<Pessoas>(chamado, HttpStatus.CREATED);
			    } catch (Exception e) {
				      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			    
			    }
			    
			    @RequestMapping(value = "/pessoas/{id}", method = RequestMethod.PUT)
			    @ResponseBody
			    public  ResponseEntity<?> pessoasUpdateById(@PathVariable String id, @RequestBody Pessoas pessoas) {
			    
			    try {
			    	
			    	if(pessoas.getId() == null) {
			    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
			    	}
			    	
					Pessoas chamado = pessoasRepository.saveAndFlush(pessoas);
							    	
			    	return new ResponseEntity<Pessoas>(chamado, HttpStatus.OK);
			    } catch (Exception e) {
				      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			    
			    }
			    
			    @RequestMapping(value = "/pessoas/{id}", method = RequestMethod.DELETE)
			    @ResponseBody
			    public  ResponseEntity<String> pessoasDeleteById(@PathVariable String id) {
			    
			    try {
					pessoasRepository.deleteById(Long.parseLong(id));
							    	
			    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
			    } catch (Exception e) {
				      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
				}
			    
			    }

	
}
