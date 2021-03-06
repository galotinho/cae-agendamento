package br.edu.ifbaiano.cae.agendamento.web.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import br.edu.ifbaiano.cae.agendamento.domain.Agendamento;
import br.edu.ifbaiano.cae.agendamento.domain.Especialidade;
import br.edu.ifbaiano.cae.agendamento.domain.Horario;
import br.edu.ifbaiano.cae.agendamento.domain.Paciente;
import br.edu.ifbaiano.cae.agendamento.domain.PerfilTipo;
import br.edu.ifbaiano.cae.agendamento.service.AgendamentoService;
import br.edu.ifbaiano.cae.agendamento.service.EmailService;
import br.edu.ifbaiano.cae.agendamento.service.EspecialidadeService;
import br.edu.ifbaiano.cae.agendamento.service.PacienteService;
import br.edu.ifbaiano.cae.agendamento.service.ProfissionalService;

@Controller
@RequestMapping("agendamentos")
public class AgendamentoController {
	
	@Autowired
	private AgendamentoService service;
	@Autowired
	private PacienteService pacienteService;
	@Autowired
	private EspecialidadeService especialidadeService;	
	@Autowired
	private ProfissionalService profissionalService;
	@Autowired
	private EmailService emailService;

	// abre a pagina de agendamento de consultas 
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'PROFISSIONAL')")
	@GetMapping({"/agendar"})
	public String agendarConsulta(ModelMap model, Agendamento agendamento) {

		model.addAttribute("especialidades", especialidadeService.buscarTodas());
		
		return "agendamento/cadastro";		
	}
	
	// salvar uma consulta agendada
	@PreAuthorize("hasAuthority('PACIENTE')")
	@PostMapping({"/salvar"})
	public ModelAndView salvar(Agendamento agendamento, RedirectAttributes attr, @AuthenticationPrincipal User user) {
		Paciente paciente = pacienteService.buscarPorUsuarioEmail(user.getUsername());
		String titulo = agendamento.getEspecialidade().getTitulo();
		Especialidade especialidade = especialidadeService
				.buscarPorTitulos(new String[] {titulo})
				.stream().findFirst().get();
		agendamento.setEspecialidade(especialidade);
		agendamento.setPaciente(paciente);
		agendamento.setHorario(profissionalService.buscarHorario(Long.valueOf(agendamento.getHorarioS())));
		
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate dataConsulta = LocalDate.parse(agendamento.getDataS(), df);
		
		agendamento.setDataConsulta(dataConsulta);	
		
		if(agendamento.getPaciente().getNome() == null) {
			ModelAndView model = new ModelAndView("error");
			model.addObject("status", 500);
			model.addObject("error", "Área Restrita");
			model.addObject("message", "Preencha seus DADOS CADASTRAIS antes de agendar uma consulta.");
			return model;
		}
		service.salvar(agendamento);
		attr.addFlashAttribute("sucesso", "Sua consulta foi agendada com sucesso.");
			
		
		return new ModelAndView("redirect:/agendamentos/agendar");		
	}
	
	// abrir pagina de historico de agendamento do paciente
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'PROFISSIONAL')")
	@GetMapping({"/historico/paciente", "/historico/consultas"})
	public String historico() {

		return "agendamento/historico-paciente";
	}
	
	// localizar o historico de agendamentos por usuario logado
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'PROFISSIONAL')")
	@GetMapping("/datatables/server/historico")
	public ResponseEntity<?> historicoAgendamentosPorPaciente(HttpServletRequest request, @AuthenticationPrincipal User user) {
		if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.PACIENTE.getDesc()))) {
			
			return ResponseEntity.ok(service.buscarHistoricoPorPacienteEmail(user.getUsername(), request));
		}
		
		if (user.getAuthorities().contains(new SimpleGrantedAuthority(PerfilTipo.PROFISSIONAL.getDesc()))) {
			
			return ResponseEntity.ok(service.buscarHistoricoPorProfissionalEmail(user.getUsername(), request));
		}		
		
		return ResponseEntity.notFound().build();
	}
	
	// localizar agendamento pelo id e envia-lo para a pagina de cadastro
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'PROFISSIONAL')")
	@GetMapping("/editar/consulta/{id}") 
	public String preEditarConsultaPaciente(@PathVariable("id") Long id, 
										    ModelMap model, @AuthenticationPrincipal User user) {
		
		Agendamento agendamento = service.buscarPorIdEUsuario(id, user.getUsername());
		
		if(agendamento.getDataConsulta().isBefore(LocalDate.now())) {
			model.addAttribute("falha", "Consulta não pode mais ser modificada. ");
			return "agendamento/historico-paciente";
		}else {
			model.addAttribute("agendamento", agendamento);
			return "agendamento/cadastro";
		}
	}
	
	@PreAuthorize("hasAuthority('PACIENTE')")
	@PostMapping("/editar")
	public String editarConsulta(Agendamento agendamento, RedirectAttributes attr, @AuthenticationPrincipal User user) {
		String titulo = agendamento.getEspecialidade().getTitulo();
		Especialidade especialidade = especialidadeService
				.buscarPorTitulos(new String[] {titulo})
				.stream().findFirst().get();
		agendamento.setEspecialidade(especialidade);
		Horario horario;
		if(agendamento.getHorarioS() != null) {
			horario = profissionalService.buscarHorario(Long.valueOf(agendamento.getHorarioS()));
		}
		else {
			horario = profissionalService.buscarHorario(agendamento.getHorario().getId());
		}
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
		LocalDate dataConsulta = LocalDate.parse(agendamento.getDataS(), df);
		
		service.editar(agendamento, user.getUsername(), horario, dataConsulta);
		attr.addFlashAttribute("sucesso", "Sua consulta foi alterada com sucesso.");
		return "redirect:/agendamentos/agendar";
	}
	
	@PreAuthorize("hasAuthority('PROFISSIONAL')")
	@PostMapping("/editar/agendamento/profissional")
	public String editarConsultaPeloProfissional(Agendamento agendamentoP, RedirectAttributes attr, @AuthenticationPrincipal User user) {
				
		if(agendamentoP.isComparecimento()) {
			service.editarPeloProfissional(agendamentoP, true);
		}else {
			service.editarPeloProfissional(agendamentoP, false);
		}
		
		attr.addFlashAttribute("sucesso", "Alteração realizada com sucesso.");
		return "redirect:/agendamentos/historico/paciente";
	}
	
	@PreAuthorize("hasAnyAuthority('PACIENTE', 'PROFISSIONAL')")
	@GetMapping("/excluir/consulta/{id}")
	public String excluirConsulta(@PathVariable("id") Long id, RedirectAttributes attr) throws MessagingException {
		Agendamento agendamento = service.buscarPorId(id);
		
		LocalDate dataConsulta = agendamento.getDataConsulta();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, dd / MMMM / yyyy");
		String data = dataConsulta.format(formatter);
		
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm");
		String horario = agendamento.getHorario().getHoraMinuto().format(dtf);
		
		String especialidade = agendamento.getEspecialidade().getTitulo();
		String destinoProfissional = agendamento.getProfissional().getUsuario().getEmail();
		String destinoPaciente = agendamento.getPaciente().getUsuario().getEmail();
		
		if(dataConsulta.isAfter(LocalDate.now())) {
			service.remover(id);
			emailService.enviarEmailCancelamentoConsulta(destinoPaciente, destinoProfissional, data, horario, especialidade);
			attr.addFlashAttribute("sucesso", "Consulta cancelada com sucesso.");
		}else {
			attr.addFlashAttribute("falha", "Consulta só pode ser cancelada com "
					+ "no mínimo 01 dia de antencedência.");
		}
		
		
		return "redirect:/agendamentos/historico/paciente";
	}

}
