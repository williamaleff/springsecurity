package com.william.springsecurity.controllers.interno;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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

import com.william.springsecurity.repositories.candidatos.CandidatosRepository;
import com.william.springsecurity.repositories.interno.InternoRepository;
import com.william.springsecurity.services.InternoService;
import com.william.springsecurity.domain.candidatos.Candidatos;
import com.william.springsecurity.domain.interno.Interno;
import com.william.springsecurity.domain.interno.InternoCandidatoDTO;;

@RestController
public class InternoController {
    
    @Autowired
	private InternoRepository internoRepository;

	@Autowired
	private InternoService internoService;

	@Autowired
	private CandidatosRepository candidatosRepository;

    private static final String UPLOAD_DIR = "uploads/";

    @GetMapping("/interno")
	  public ResponseEntity<List<InternoCandidatoDTO>> getAllInterno(
	        @RequestParam(required = false, name="nome_like") String title,
	        @RequestParam(defaultValue = "1", name="_page") int page,
	        @RequestParam(defaultValue = "3", name="_limit") int size
	      ) {

			try {
				Pageable paging = PageRequest.of(page - 1, size);
				Page<Interno> pageInterno;
				HttpHeaders headers = new HttpHeaders();
		
				if (title == null) {
					pageInterno = internoRepository.findAll(paging);
					headers.add("x-total-count", String.valueOf(internoService.getTotalCount()));
				} else {
					List<Interno> allInternos = internoRepository.findByNameContaining(title);
					int start = (int) paging.getOffset();
					int end = Math.min(start + paging.getPageSize(), allInternos.size());
					List<Interno> pageContent = allInternos.subList(start, end);
					headers.add("x-total-count", String.valueOf(allInternos.size()));
					pageInterno = new PageImpl<>(pageContent, paging, allInternos.size());
				}
		
				List<InternoCandidatoDTO> dtos = new ArrayList<>();
				for (Interno interno : pageInterno.getContent()) {
					InternoCandidatoDTO dto = new InternoCandidatoDTO();
		
					// Dados de Interno
					dto.setId(interno.getId());
					dto.setNome(interno.getNome());
					dto.setProntuario(interno.getProntuario());
					dto.setDigital(interno.getDigital());
					dto.setFoto(interno.getFoto());
					
					// Busca dados adicionais em Candidatos
					Optional<Candidatos> candidatoOpt = candidatosRepository.findByProntuario(interno.getProntuario());
					if (candidatoOpt.isPresent()) {
						Candidatos candidato = candidatoOpt.get();
						dto.setFuncao(candidato.getFuncao());
						dto.setLocalizacao(candidato.getUltimaLocalizacao());
						dto.setMae(candidato.getMae());
						dto.setRegime(candidato.getTipoDeRegime());
						dto.setUnidade(candidato.getUnidade());

					}
		
					dtos.add(dto);
				}
		
				return ResponseEntity.ok().headers(headers).body(dtos);
	    } catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
	    }
	  }
	 
	  @RequestMapping(value = "/interno/{id}", method = RequestMethod.GET)
	  @ResponseBody
	  public ResponseEntity<InternoCandidatoDTO> internoById(@PathVariable String id) {
		  try {
			  Optional<Interno> internoOpt = internoRepository.findById(Long.parseLong(id));
			  if (internoOpt.isPresent()) {
				  Interno interno = internoOpt.get();
				  InternoCandidatoDTO dto = new InternoCandidatoDTO();
	  
				  // Dados de Interno
				  dto.setId(interno.getId());
				  dto.setNome(interno.getNome());
				  dto.setProntuario(interno.getProntuario());
				  dto.setDigital(interno.getDigital());
				  dto.setFoto(interno.getFoto());
	  
				  // Busca dados adicionais em Candidatos usando o prontuário do Interno
				  Optional<Candidatos> candidatoOpt = candidatosRepository.findByProntuario(interno.getProntuario());
				  if (candidatoOpt.isPresent()) {
					  Candidatos candidato = candidatoOpt.get();
					  dto.setFuncao(candidato.getFuncao());
					  dto.setLocalizacao(candidato.getUltimaLocalizacao());
					  dto.setMae(candidato.getMae());
					  dto.setRegime(candidato.getTipoDeRegime());
					  dto.setUnidade(candidato.getUnidade());
				  }
	  
				  return new ResponseEntity<>(dto, HttpStatus.OK);
			  } else {
				  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			  }
		  } catch (Exception e) {
			  return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		  }
	  }
		  
	  @PostMapping(value = "interno")
	  @ResponseBody
	  public ResponseEntity<InternoCandidatoDTO> salvar(@RequestBody InternoCandidatoDTO dto) {
		  try {
			  // --- Processamento da entidade Interno ---
			  // Cria (ou atualiza) o objeto Interno com os dados recebidos
			  Interno interno = new Interno();
			  // Se for atualização, você pode verificar se o dto.getId() não é nulo e buscar o registro existente.
			  // Aqui, estamos tratando como criação.
			  interno.setNome(dto.getNome());
			  interno.setProntuario(dto.getProntuario());
			  interno.setDigital(dto.getDigital());
			  interno.setFoto(dto.getFoto());
			  // Atualiza a data da atualização
			  interno.setDataDaAtualizacao(LocalDateTime.now());
			  
			  // Salva o Interno no banco
			  Interno savedInterno = internoRepository.save(interno);
			  
			  // --- Processamento da entidade Candidatos ---
			  // Verifica se já existe um Candidato com o mesmo prontuário
			  Optional<Candidatos> candidatoOpt = candidatosRepository.findByProntuario(dto.getProntuario());
			  Candidatos candidato;
			  if (candidatoOpt.isPresent()) {
				  // Se já existe, atualiza os campos desejados
				  candidato = candidatoOpt.get();
			  } else {
				  // Se não existir, cria um novo objeto
				  candidato = new Candidatos();
				  candidato.setProntuario(dto.getProntuario());
			  }
			  
			  // Atualiza os campos vindos do DTO (que não estão na entidade Interno)
			  candidato.setMae(dto.getMae());
			  candidato.setFuncao(dto.getFuncao());
			  candidato.setUltimaLocalizacao(dto.getLocalizacao());
			  candidato.setTipoDeRegime(dto.getRegime());
			  candidato.setUnidade(dto.getUnidade());
			  candidato.setBiometria("sim");
			  
			  // Salva (ou atualiza) o registro de Candidatos
			  Candidatos savedCandidato = candidatosRepository.save(candidato);
			  
			  // --- Prepara o DTO de retorno ---
			  InternoCandidatoDTO retorno = new InternoCandidatoDTO();
			  // Dados de Interno
			  retorno.setId(savedInterno.getId());
			  retorno.setNome(savedInterno.getNome());
			  retorno.setProntuario(savedInterno.getProntuario());
			  retorno.setDigital(savedInterno.getDigital());
			  retorno.setFoto(savedInterno.getFoto());
			  
			  // Dados de Candidatos
			  retorno.setFuncao(savedCandidato.getFuncao());
			  retorno.setLocalizacao(savedCandidato.getUltimaLocalizacao());
			  retorno.setMae(savedCandidato.getMae());
			  retorno.setRegime(savedCandidato.getTipoDeRegime());
			  retorno.setUnidade(savedCandidato.getUnidade());
			  
			  return new ResponseEntity<>(retorno, HttpStatus.CREATED);
			  
		  } catch (Exception e) {
			  return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		  }
	  }
	  
	  @RequestMapping(value = "/interno/{id}", method = RequestMethod.PUT)
	  @ResponseBody
	  public ResponseEntity<InternoCandidatoDTO> internoUpdateById(@PathVariable String id, 
			  @RequestBody InternoCandidatoDTO dto) {
		  try {
			  // Verifica se o ID está presente no DTO
			  if(dto.getId() == null) {
				  return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			  }
			  
			  // Busca o Interno pelo ID informado na URL
			  Optional<Interno> internoOpt = internoRepository.findById(Long.parseLong(id));
			  if (!internoOpt.isPresent()) {
				  return new ResponseEntity<>(HttpStatus.NOT_FOUND);
			  }
			  
			  Interno interno = internoOpt.get();
			  // Atualiza os campos de Interno com os dados do DTO
			  interno.setNome(dto.getNome());
			  interno.setProntuario(dto.getProntuario());
			  interno.setDigital(dto.getDigital());
			  interno.setFoto(dto.getFoto());
			  // Atualiza a data de atualização para o momento atual
			  interno.setDataDaAtualizacao(LocalDateTime.now());
			  
			  // Salva a atualização de Interno
			  Interno updatedInterno = internoRepository.saveAndFlush(interno);
			  
			  // Atualiza ou cria o registro de Candidatos com base no prontuário
			  Optional<Candidatos> candidatoOpt = candidatosRepository.findByProntuario(dto.getProntuario());
			  Candidatos candidato;
			  if(candidatoOpt.isPresent()) {
				  candidato = candidatoOpt.get();
			  } else {
				  candidato = new Candidatos();
				  candidato.setProntuario(dto.getProntuario());
			  }
			  // Atualiza os campos de Candidatos vindos do DTO
			  candidato.setMae(dto.getMae());
			  candidato.setFuncao(dto.getFuncao());
			  candidato.setUltimaLocalizacao(dto.getLocalizacao());
			  candidato.setTipoDeRegime(dto.getRegime());
			  candidato.setUnidade(dto.getUnidade());
			  
			  Candidatos updatedCandidato = candidatosRepository.saveAndFlush(candidato);
			  
			  // Monta o DTO de retorno com os dados atualizados
			  InternoCandidatoDTO retorno = new InternoCandidatoDTO();
			  // Dados de Interno
			  retorno.setId(updatedInterno.getId());
			  retorno.setNome(updatedInterno.getNome());
			  retorno.setProntuario(updatedInterno.getProntuario());
			  retorno.setDigital(updatedInterno.getDigital());
			  retorno.setFoto(updatedInterno.getFoto());
			  // Dados de Candidatos
			  retorno.setMae(updatedCandidato.getMae());
			  retorno.setFuncao(updatedCandidato.getFuncao());
			  retorno.setLocalizacao(updatedCandidato.getUltimaLocalizacao());
			  retorno.setRegime(updatedCandidato.getTipoDeRegime());
			  retorno.setUnidade(updatedCandidato.getUnidade());
			  
			  return new ResponseEntity<>(retorno, HttpStatus.OK);
		  } catch (Exception e) {
			  return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		  }
	  }
	  
	    
	    @RequestMapping(value = "/interno/{id}", method = RequestMethod.DELETE)
	    @ResponseBody
	    public  ResponseEntity<String> internoDeleteById(@PathVariable String id) {
	    
	    try {
			Long internoId = Long.parseLong(id);
        
        // Recupera o Interno antes de deletar
        Optional<Interno> internoOptional = internoRepository.findById(internoId);
		if (internoOptional.isPresent()) {
            Interno interno = internoOptional.get();
            
            // Busca o candidato com o mesmo prontuário do Interno
            Optional<Candidatos> candidatoOptional = candidatosRepository.findByProntuario(interno.getProntuario());
            if (candidatoOptional.isPresent()) {
                Candidatos candidato = candidatoOptional.get();
                // Atualiza o valor de 'biometria' para "não"
                candidato.setBiometria("nao");
                candidatosRepository.save(candidato);
            }
            
            // Realiza a exclusão do Interno
            internoRepository.deleteById(internoId);
            
            return new ResponseEntity<>("User deletado com sucesso", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Interno não encontrado", HttpStatus.NOT_FOUND);
        }
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
