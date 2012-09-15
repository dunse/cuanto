/*
 Copyright (c) 2010 Todd Wells

This file is part of Cuanto, a test results repository and analysis program.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

*/
package cuanto

public class CuantoQuery {
	String hql
	List positionalParameters
	Map paginateParameters

	@Override
	public String toString()
	{
		return "CuantoQuery{" +
			"hql='" + hql + '\'' +
			", positionalParameters=" + positionalParameters +
			", paginateParameters=" + paginateParameters +
			'}';
	}

	@Override
	public boolean equals(o)
	{
		if (is(o)) return true
		if (getClass() != o.class) return false

		CuantoQuery that = (CuantoQuery) o

		if (hql != that.hql) return false
		if (paginateParameters != that.paginateParameters) return false
		if (positionalParameters != that.positionalParameters) return false

		return true
	}

	@Override
	public int hashCode()
	{
		int result
		result = (hql != null ? hql.hashCode() : 0)
		result = 31 * result + (positionalParameters != null ? positionalParameters.hashCode() : 0)
		result = 31 * result + (paginateParameters != null ? paginateParameters.hashCode() : 0)
		return result
	}
}