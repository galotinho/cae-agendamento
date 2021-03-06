package br.edu.ifbaiano.cae.agendamento.repository.projection;

public class RelatorioEspecialidade {

	private String especialidade;
	private int totalConsultasAgendadas;
	private int totalConsultasRealizadas;
	private Double percNaoComparecimento;
	
	public RelatorioEspecialidade() {
		
	}
	
	public String getEspecialidade() {
		return especialidade;
	}
	public void setEspecialidade(String especialidade) {
		this.especialidade = especialidade;
	}
	
	public int getTotalConsultasAgendadas() {
		return totalConsultasAgendadas;
	}
	public void setTotalConsultasAgendadas(int totalConsultasAgendadas) {
		this.totalConsultasAgendadas = totalConsultasAgendadas;
	}
	public int getTotalConsultasRealizadas() {
		return totalConsultasRealizadas;
	}
	public void setTotalConsultasRealizadas(int totalConsultasRealizadas) {
		this.totalConsultasRealizadas = totalConsultasRealizadas;
	}
	public Double getPercNaoComparecimento() {
		return percNaoComparecimento;
	}
	public void setPercNaoComparecimento(Double percNaoComparecimento) {
		this.percNaoComparecimento = percNaoComparecimento;
	}
	
	
}
