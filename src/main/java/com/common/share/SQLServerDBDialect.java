package com.common.share;

import java.sql.Types;

import org.hibernate.Hibernate;
import org.hibernate.dialect.SQLServerDialect;

public class SQLServerDBDialect extends SQLServerDialect
{
	public SQLServerDBDialect()
	{
		super();
		registerHibernateType(Types.NVARCHAR, Hibernate.STRING.getName());
	}
}