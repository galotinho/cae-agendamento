package br.edu.ifbaiano.cae.agendamento.web.exception;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import br.edu.ifbaiano.cae.agendamento.exception.AcessoNegadoException;
import br.edu.ifbaiano.cae.agendamento.exception.ConsultaSemNomePacienteException;

@ControllerAdvice
public class ExceptionController {

	@ExceptionHandler(UsernameNotFoundException.class)
	public ModelAndView usuarioNaoEncontradoException(UsernameNotFoundException ex){
		
		ModelAndView model = new ModelAndView("error");
		model.addObject("status", 404);
		model.addObject("error", "Operação não pode ser realizada!");
		model.addObject("message", ex.getMessage());
		return model;
	}
	
	@ExceptionHandler(AcessoNegadoException.class)
	public ModelAndView acessoNegadoException(AcessoNegadoException ex){
		
		ModelAndView model = new ModelAndView("error");
		model.addObject("status", 403);
		model.addObject("error", "Operação não pode ser realizada!");
		model.addObject("message", ex.getMessage());
		return model;
	}
	
	@ExceptionHandler(ConsultaSemNomePacienteException.class)
	public ModelAndView ConsultaSemNomePaciente(ConsultaSemNomePacienteException ex){
		
		ModelAndView model = new ModelAndView("error");
		model.addObject("status", 500);
		model.addObject("error", "Operação não pode ser realizada!");
		model.addObject("message", ex.getMessage());
		return model;
	}
}
