package com.common.share;

import java.text.DecimalFormat;

public class CommaSeparator
{
	private DecimalFormat dfmt = new DecimalFormat("#0.000");
	private String t, totalFinal;
	private String totalAmt;

	public String setComma(double amount)
	{	
		double tamount = Math.abs(amount);
		totalFinal = "";
		char tArray[] = new char[90];
		char tArray2[] = new char[90];
		int len;
		int j = 0;
		totalAmt = String.valueOf(dfmt.format(tamount));
		len = totalAmt.length();
		
		for (int i = len-1; i >= 0; i--)
		{
			tArray[j] = totalAmt.charAt(i);
			if (j == 6)
			{
				j++;
				tArray[j]=',';
			}

			if (j == 10 || j == 14)
			{
				j++;
				tArray[j] = ',';
			}
			j++;
		}
		t = String.valueOf(tArray, 0, j);
		len = t.length();
		j = 0;
		for (int i = len-1; i >= 0; i--)
		{
			tArray2[j] = t.charAt(i);
			j++;
		}

		totalFinal = String.valueOf(tArray2, 0,j);
		if(totalFinal.charAt(0) == ',')
		{
			totalFinal = totalFinal.substring(1,totalFinal.length());
		}
		tamount = 0.0;
		//txtTotalQty.setValue(totalFinal);
		if (amount < 0)
			return "-" + totalFinal;
		else
			return totalFinal;
	}
}
