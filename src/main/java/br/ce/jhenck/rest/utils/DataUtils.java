package br.ce.jhenck.rest.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DataUtils {

	public static String getDataDiferencaDias(Integer qtdDias, String tipoData) throws Exception {
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DAY_OF_MONTH, qtdDias);
		return getDataFormatada(cal.getTime(), tipoData);
	}

	public static String getDataFormatada(Date data, String tipoData) throws Exception{
		String formatoData;
		if (tipoData == "BR") {
			SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
			formatoData = format.format(data);
		} else if (tipoData == "GMT") {
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			formatoData = format.format(data);
		} else {
			throw new Exception("Formato não configurado");
		}
		return formatoData;
	}
}
