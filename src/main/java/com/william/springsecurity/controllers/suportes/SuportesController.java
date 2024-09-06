package com.william.springsecurity.controllers.suportes;

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

import com.william.springsecurity.domain.suportes.Suportes;
import com.william.springsecurity.repositories.suportes.SuportesRepository;

@RestController
public class SuportesController {

    @Autowired
	private SuportesRepository suporteRepository;
	
    @GetMapping("/suporte")
		  public ResponseEntity<List<Suportes>> getAllSuporte(
		        @RequestParam(required = false, name="descricao_like") String title,
		        @RequestParam(required = false, name="data_like") String date,
		        @RequestParam(defaultValue = "1", name="_page") int page,
		        @RequestParam(defaultValue = "3", name="_limit") int size
		      ) {

		    try {
		      List<Suportes> suporte = new ArrayList<Suportes>();
		      Pageable paging = PageRequest.of((page-1), size);
		      
		      Page<Suportes> pageTuts;
		      
		      if (title == null && date == null) {
		        pageTuts = suporteRepository.findAll(paging);
		      }
		      else if(title != null && date == null) {
		    	  
		    	    List<Suportes> allCustomers = suporteRepository.findByDescricaoContaining(title);
				    int start = (int) paging.getOffset();
				    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

				    List<Suportes> pageContent = allCustomers.subList(start, end);
				    

		        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());
		      } 
		      else if(title == null && date != null) {
		    	  
		    	List<Suportes> allCustomers = suporteRepository.findByDateContaining(date);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Suportes> pageContent = allCustomers.subList(start, end);
			    

	            pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());
   
		      } else {
		    	  	List<Suportes> allCustomers = suporteRepository.findByDateAndDescricaoContaining(date, title);
				    int start = (int) paging.getOffset();
				    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

				    List<Suportes> pageContent = allCustomers.subList(start, end);
				    

		            pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());

		      }
		      
		      suporte = pageTuts.getContent();

		      List<Suportes> response = suporte;
		      
		      return new ResponseEntity<>(response, HttpStatus.OK);
		    } catch (Exception e) {
		      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
		    }
		  }
		 
		    @RequestMapping(value = "/suporte/{id}", method = RequestMethod.GET)
		    @ResponseBody
		    public  ResponseEntity<Suportes> suporteById(@PathVariable String id) {
		    
		    try {
				Suportes suporte = suporteRepository.findById(Long.parseLong(id)).get();
						    	
		    	return new ResponseEntity<Suportes>(suporte, HttpStatus.OK);
		    } catch (Exception e) {
			      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		    
		    }
		    
		    @PostMapping(value = "suporte") //mapeia a url
		    @ResponseBody //descrição da resposta
		    public ResponseEntity<Suportes> salvarSuporte(@RequestBody Suportes suporte) { //Reebe os dados para salvar
		    try {
		    	Suportes chamado = suporteRepository.save(suporte);
		    	
		    	return new ResponseEntity<Suportes>(chamado, HttpStatus.CREATED);
		    } catch (Exception e) {
			      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		    
		    }
		    
		    @RequestMapping(value = "/suporte/{id}", method = RequestMethod.PUT)
		    @ResponseBody
		    public  ResponseEntity<?> suporteUpdateById(@PathVariable String id, @RequestBody Suportes suporte) {
		    
		    try {
		    	
		    	if(suporte.getId() == null) {
		    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
		    	}
		    	
				Suportes chamado = suporteRepository.saveAndFlush(suporte);
						    	
		    	return new ResponseEntity<Suportes>(chamado, HttpStatus.OK);
		    } catch (Exception e) {
			      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		    
		    }
		    
		    @RequestMapping(value = "/suporte/{id}", method = RequestMethod.DELETE)
		    @ResponseBody
		    public  ResponseEntity<String> suporteDeleteById(@PathVariable String id) {
		    
		    try {
				suporteRepository.deleteById(Long.parseLong(id));
						    	
		    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
		    } catch (Exception e) {
			      return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
			}
		    
		    }

}
