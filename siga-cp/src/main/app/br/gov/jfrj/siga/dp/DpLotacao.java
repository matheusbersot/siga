/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/*
 * Criado em  21/12/2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package br.gov.jfrj.siga.dp;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Formula;

import br.gov.jfrj.siga.base.Texto;
import br.gov.jfrj.siga.dp.dao.CpDao;
import br.gov.jfrj.siga.model.Assemelhavel;
import br.gov.jfrj.siga.model.Historico;
import br.gov.jfrj.siga.model.Selecionavel;
import br.gov.jfrj.siga.sinc.lib.Desconsiderar;
import br.gov.jfrj.siga.sinc.lib.Sincronizavel;
import br.gov.jfrj.siga.sinc.lib.SincronizavelSuporte;

@Entity
@Table(name = "DP_LOTACAO", schema = "CORPORATIVO")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class DpLotacao extends AbstractDpLotacao implements Serializable,
		Selecionavel, Historico, Sincronizavel,  Comparable  {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5628179687234082413L;

	@Formula(value = "REMOVE_ACENTO(NOME_LOTACAO)")
	@Desconsiderar
	private String nomeLotacaoAI;

	public DpLotacao() {
		super();
	}

	public boolean isFechada() {
		if (getDataFimLotacao() == null)
			return false;
		DpLotacao lotIni = getLotacaoInicial();
		Set<DpLotacao> setLotas = lotIni.getLotacoesPosteriores();
		if (setLotas != null)
			for (DpLotacao l : setLotas)
				if (l.getDataFimLotacao() == null)
					return false;

		return true;
	}

	public String getLocalidadeString() {
		String s[] = getNomeLotacao().toLowerCase().split(" de ");
		String s2 = "";
		if (s.length >= 2) {
			s2 = s[s.length - 1].toLowerCase();
			if (CpLocalidade.MUNICIPIOS.contains(s2))
				return s2;
			// Fix:
			s2 = s[s.length - 2] + " de " + s[s.length - 1];
			if (CpLocalidade.MUNICIPIOS.contains(s2))
				return s2;
		}
		return getOrgaoUsuario().getMunicipioOrgaoUsu();

	}

	public String iniciais(String s) {
		final StringBuilder sb = new StringBuilder(10);
		boolean f = true;

		s = s.replace(" E ", " ");
		s = s.replace(" DA ", " ");
		s = s.replace(" DE ", " ");
		s = s.replace(" DO ", " ");

		for (int i = 0; i < s.length(); i++) {
			final char c = s.charAt(i);
			if (f) {
				sb.append(c);
				f = false;
			}
			if (c == ' ') {
				f = true;
			}
		}
		return sb.toString();
	}

	public String getIniciais() {
		return iniciais(getNomeLotacao());
	}

	public Long getId() {
		return super.getIdLotacao();
	}
	
	public void setId(Long id){
		setIdLotacao(id);
	}

	public Long getIdLotacaoPai() {
		if (getLotacaoPai() == null)
			return null;
		return Long.valueOf(getLotacaoPai().getIdLotacao());
	}

	public String getSigla() {
		String s = getSiglaLotacao();
		if (isFechada())
			s += " (extinta)";
		return s;
	}

	public String getSiglaCompleta() {
		String s = getOrgaoUsuario().getSiglaOrgaoUsu() + getSiglaLotacao();
		return s;
	}

	public void setSigla(String sigla) {
		String siglasOrgaoUsu = "";
		List<CpOrgaoUsuario> lou = CpDao.getInstance()
				.listarOrgaosUsuarios();
		for (CpOrgaoUsuario ou : lou) {
			siglasOrgaoUsu += (siglasOrgaoUsu.length() > 0 ? "|" : "")
					+ ou.getSiglaOrgaoUsu();
		}
		final Pattern p1 = Pattern.compile("^(" + siglasOrgaoUsu + ")(.+)");
		final Matcher m = p1.matcher(sigla.toUpperCase());
		if (m.find()) {
			CpOrgaoUsuario cpOrgao = new CpOrgaoUsuario();
			cpOrgao.setSiglaOrgaoUsu(m.group(1).toUpperCase());
			setOrgaoUsuario(cpOrgao);
			setSiglaLotacao(m.group(2).toUpperCase());
		}
		else {
			setSiglaLotacao(sigla.toUpperCase());
		}
	}

	/*
	 * public String getSiglaLotacao() { if (getOrgaoUsuario() != null) return
	 * getOrgaoUsuario().getAcronimoOrgaoUsu() + "/" + super.getSiglaLotacao();
	 * return super.getSiglaLotacao(); }
	 */

	// TAH: Foi necessario criar essa funcao para resolver um problema
	// especifico da SJRJ-SRH. Optamos por fazer dessa forma por acreditarmos
	// que no futuro essa necessidade seja revista.
	public String getSiglaAmpliada() {
		if (getOrgaoUsuario().getIdOrgaoUsu() == 1L && isSubsecretaria())
			return getSiglaLotacao() + " Dire��o";
		return getSiglaLotacao();
	}

	// public void setSigla(final String sigla) {
	// setSiglaLotacao(sigla);
	// }

	public String getDescricao() {
		return getNomeLotacao();
	}

	public String getDescricaoAmpliada() {
		if (getOrgaoUsuario().getIdOrgaoUsu() == 1L && isSubsecretaria())
			return "Dire��o da " + getNomeLotacao();
		return getNomeLotacao();
	}

	public String getDescricaoIniciaisMaiusculas() {
		return Texto.maiusculasEMinusculas(getDescricao());
	}

	public String getDescricaoHierarquia() {
		String strHierarquia = new String();
		DpLotacao lot = getLotacaoPai();
		while (lot != null) {
			strHierarquia = lot.getSiglaLotacao() + strHierarquia;
			strHierarquia = ", " + strHierarquia;
			lot = lot.getLotacaoPai();
		}
		if (strHierarquia.length() > 0) {
			strHierarquia = strHierarquia.substring(2, strHierarquia.length());
			strHierarquia = "(" + strHierarquia;
			strHierarquia = strHierarquia + ")";
		}
		return getNomeLotacao() + strHierarquia;
	}

	public String getNomeLotacaoAI() {
		return nomeLotacaoAI;
	}

	public void setNomeLotacaoAI(String nomeLotacaoAI) {
		this.nomeLotacaoAI = nomeLotacaoAI;
	}

	public boolean equivale(Object other) {
		if (other == null)
			return false;
		return this.getIdInicial().longValue() == ((DpLotacao) other)
				.getIdInicial().longValue();
	}

	public String getNomeMaiusculas() {
		return getNomeLotacao().toUpperCase();
	}

	public Long getIdInicial() {
		// TODO Auto-generated method stub
		return getIdLotacaoIni().longValue();
	}

	public Boolean isSubsecretaria() {
		return getSiglaLotacao().length() == 3;
	}

	public DpLotacao getLotacaoPorNivelMaximo(int iNivelMaximo) {
		int iNivel = 1;
		DpLotacao lot = this;

		while (lot.getLotacaoPai() != null) {
			lot = lot.getLotacaoPai();
			iNivel++;
		}
		if (iNivel <= iNivelMaximo) {
			return this;
		}

		lot = this;
		for (; iNivel > iNivelMaximo; iNivel--) {
			lot = lot.getLotacaoPai();
		}
		return lot;
	}

	// M�todos necess�rios para ser "Sincronizavel"
	//
	public Date getDataFim() {
		return getDataFimLotacao();
	}

	public Date getDataInicio() {
		return getDataInicioLotacao();
	}

	public String getDescricaoExterna() {
		return getDescricaoAmpliada();
	}

	public String getIdExterna() {
		return getIdeLotacao();
	}

	public String getLoteDeImportacao() {
		return getOrgaoUsuario().getId().toString();
	}

	public int getNivelDeDependencia() {
		return SincronizavelSuporte.getNivelDeDependencia(this);
	}

	public void setDataFim(Date dataFim) {
		setDataFimLotacao(dataFim);
	}

	public void setDataInicio(Date dataInicio) {
		setDataInicioLotacao(dataInicio);
	}

	public void setIdExterna(String idExterna) {
		setIdeLotacao(idExterna);
	}

	public void setIdInicial(Long idInicial) {
		setIdLotacaoIni(idInicial);
	}

	public void setLoteDeImportacao(String loteDeImportacao) {
	}

	public boolean semelhante(Assemelhavel obj, int profundidade) {
		return SincronizavelSuporte.semelhante(this, obj, profundidade);
	}

	/**
	 * retorna se ativo na data
	 * 
	 * @param dt
	 *            data quando ser saber se estava ativa
	 * @return true or false
	 */
	public boolean ativaNaData(Date dt) {
		if (dt == null)
			return this.getDataFim() == null;
		if (dt.before(this.getDataInicio()))
			return false;
		// dt >= DtIni
		if (this.getDataFim() == null)
			return true;
		if (dt.before(this.getDataFim()))
			return true;
		return false;
	}

	/**
	 * Retorna a lota��o atual no hist�rico desta lota��o
	 * 
	 * @return DpLotacao
	 */
	public DpLotacao getLotacaoAtual() {
		DpLotacao lotIni = getLotacaoInicial();
		Set<DpLotacao> setLotas = lotIni.getLotacoesPosteriores();
		if (setLotas != null)
			for (DpLotacao l : setLotas){
				if(l.getDataFim()==null){
					return l;
				}
				
			}
				

		return this;
	}

	public Long getHisIdIni() {
		return getIdLotacaoIni();
	}

	public void setHisIdIni(Long hisIdIni) {
		setIdLotacaoIni(hisIdIni);
	}

	public Date getHisDtIni() {
		return getDataInicioLotacao();
	}

	public void setHisDtIni(Date hisDtIni) {
		setDataInicio(hisDtIni);
	}

	public Date getHisDtFim() {
		return getDataFimLotacao();
	}

	public void setHisDtFim(Date hisDtFim) {
		setDataFimLotacao(hisDtFim);
	}

	public Integer getHisAtivo() {
		return getDataFimLotacao() != null ? 1 : 0;
	}

	public void setHisAtivo(Integer hisAtivo) {
		//
	}

	/**
	 * 
	 * @return o id do �rg�o do usu�rio
	 */
	public Long getIdOrgaoUsuario() {
		Long idOrgaoUsuario = null;
		CpOrgaoUsuario orgaoUsuario = super.getOrgaoUsuario();
		if (orgaoUsuario != null) {
			idOrgaoUsuario = orgaoUsuario.getId();
		}
		return idOrgaoUsuario;
	}
	
	 /**
     * Retorna a data de in�cio da lota��o no formato dd/mm/aa HH:MI:SS,
     * por exemplo, 01/02/10 14:10:00.
     * 
     * @return Data de in�cio da pessoa no formato dd/mm/aa HH:MI:SS, por
     *         exemplo, 01/02/10 14:10:00.
     * 
     */
    public String getDtInicioLotacaoDDMMYYHHMMSS() {
            if (getDataInicioLotacao() != null) {
                    final SimpleDateFormat df = new SimpleDateFormat(
                                    "dd/MM/yy HH:mm:ss");
                    return df.format(getDataInicioLotacao());
            }
            return "";
    }

    /**
     * Retorna a data de fim da lota��o no formato dd/mm/aa HH:MI:SS,
     * por exemplo, 01/02/10 14:10:00.
     * 
     * @return Data de in�cio da fim no formato dd/mm/aa HH:MI:SS, por
     *         exemplo, 01/02/10 14:10:00.
     * 
     */
    public String getDtFimLotacaoDDMMYYHHMMSS() {
            if (getDataFimLotacao() != null) {
                    final SimpleDateFormat df = new SimpleDateFormat(
                                    "dd/MM/yy HH:mm:ss");
                    return df.format(getDataFimLotacao());
            }
            return "";
    }       
    
    public int compareTo(Object o) {
            DpLotacao other = (DpLotacao) o;

            return getNomeLotacao().compareTo(other.getNomeLotacao());
    }
    
	@Override
	public String toString() {
		return getSiglaCompleta();
	}

}