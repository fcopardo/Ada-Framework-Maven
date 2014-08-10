/**
   Copyright Mob&Me 2012 (@MobAndMe)

   Licensed under the LGPL Lesser General Public License, Version 3.0 (the "License"),  
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.gnu.org/licenses/lgpl.html

   Unless required by applicable law or agreed to in writing, software 
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
   
   Website: http://adaframework.com
   Contact: Txus Ballesteros <txus.ballesteros@mobandme.com>
*/

package com.mobandme.ada.validators;

import java.lang.reflect.Field;

/**
 * This class represent the result of an individual validation process.
 * @version 2.4.3
 * @author Mob&Me
 */
public final class ValidationResult {
	
	private Boolean    isOK = false;
	private String     message = "";
	private Field      field;
	
	public Field getField() { return field; }
	public void setField(Field pField) { field = pField; }
	
	public Boolean IsOK() { return this.isOK; }
	public void IsOK(Boolean pValue) { this.isOK = pValue; }

	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
}
