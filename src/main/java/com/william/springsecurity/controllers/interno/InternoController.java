package com.william.springsecurity.controllers.interno;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.io.File;

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
import org.springframework.web.multipart.MultipartFile;

import com.william.springsecurity.repositories.interno.InternoRepository;
import com.william.springsecurity.services.InternoService;
import com.william.springsecurity.domain.interno.Interno;;

@RestController
public class InternoController {
    
    @Autowired
	private InternoRepository internoRepository;

	@Autowired
	private InternoService internoService;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/interno")
	  public ResponseEntity<List<Interno>> getAllInterno(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

	    try {
	      List<Interno> interno = new ArrayList<Interno>();
	      Pageable paging = PageRequest.of((page-1), size);
	      
	      Page<Interno> pageTuts;
		  HttpHeaders headers = new HttpHeaders();

	      if (title == null) {
	        pageTuts = internoRepository.findAll(paging);
			headers.add("x-total-count", String.valueOf(internoService.getTotalCount()) );
  
	      }else {
	    	    List<Interno> allCustomers = internoRepository.findByNameContaining(title);
			    int start = (int) paging.getOffset();
			    int end = Math.min((start + paging.getPageSize()), allCustomers.size());

			    List<Interno> pageContent = allCustomers.subList(start, end);
				headers.add("x-total-count", String.valueOf(allCustomers.size()) );

	        pageTuts = new PageImpl<>(pageContent, paging, allCustomers.size());;
	      }
	      interno = pageTuts.getContent();

	      List<Interno> response = interno;

	      return new ResponseEntity<>(response, headers,HttpStatus.OK);
	    } catch (Exception e) {
	      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
	    }
	  }
	 
	    @RequestMapping(value = "/interno/{id}", method = RequestMethod.GET)
	    @ResponseBody
	    public  ResponseEntity<Interno> internoById(@PathVariable String id) {
	    
	    try {
			Interno interno = internoRepository.findById(Long.parseLong(id)).get();
					    	
	    	return new ResponseEntity<Interno>(interno, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @PostMapping(value = "interno") //mapeia a url
	    @ResponseBody //descrição da resposta
	    public ResponseEntity<Interno> salvar(@RequestBody Interno interno) { //Reebe os dados para salvar
	    try {
	    	Interno chamado = internoRepository.save(interno);
	    	
	    	return new ResponseEntity<Interno>(chamado, HttpStatus.CREATED);
	    } catch (Exception e) {
		    return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/interno/{id}", method = RequestMethod.PUT)
	    @ResponseBody
	    public  ResponseEntity<?> internoUpdateById(@PathVariable String id, @RequestBody Interno interno) {
	    
	    try {
	    	
	    	if(interno.getId() == null) {
	    		return new ResponseEntity<String>("Id não foi informado para atualização.", HttpStatus.OK);
	    	}
	    	
			Interno chamado = internoRepository.saveAndFlush(interno);
					    	
	    	return new ResponseEntity<Interno>(chamado, HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }
	    
	    @RequestMapping(value = "/interno/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> internoDeleteById(@PathVariable String id) {
	    
	    try {
			internoRepository.deleteById(Long.parseLong(id));
					    	
	    	return new ResponseEntity<String>("User deletado com sucesso", HttpStatus.OK);
	    } catch (Exception e) {
		      return new ResponseEntity<>(null, null, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	    
	    }

        ///////////////////UPLOAD//////////////////////////////////
        
        @PostMapping(value = "/upload")
        @ResponseBody //descrição da resposta
        public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {

        if (file.isEmpty()) {
             // Retorna um JSON com a mensagem de erro
        return ResponseEntity.badRequest().body(
            new UploadResponseDTO("Por favor, selecione um arquivo para upload."));   
        }

        try {
            // Cria o diretório de upload se não existir
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Obtém o nome original do arquivo e define o caminho onde será salvo
            String fileName = file.getOriginalFilename();
            Path filePath = Paths.get(UPLOAD_DIR, fileName);

            // Copia o conteúdo do arquivo para o destino, substituindo se já existir
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            // Construa a URL para acesso ao arquivo.
            // Atenção: adapte a URL de acordo com sua configuração (domínio, porta, etc.)
            String fileUrl = "http://localhost:8989/uploads/" + fileName;

            // Retorna um objeto JSON contendo a propriedade "url"
            return ResponseEntity.ok(new UploadResponseDTO(fileUrl));

            } catch (IOException e) {
                e.printStackTrace();
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                     .body(new UploadResponseDTO("Erro ao fazer upload do arquivo: " + e.getMessage()));
            }
    }

}
