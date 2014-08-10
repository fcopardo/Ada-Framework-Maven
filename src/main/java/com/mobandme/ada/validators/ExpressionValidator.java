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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.mobandme.ada.Entity;
import com.mobandme.ada.annotations.RegularExpressionValidation;

/**
 * This class implement the logic of the Regular Expression Validations. 
 * @version 2.4.3
 * @author Mob&Me
 */
public class ExpressionValidator extends Validator {

	@Override
	public Boolean Validate(Entity pEntity, Field pField, Object pAnnotation, Object pValue) {
		Boolean returnedValue = true;
		
		if (pValue != null) {
			if (pAnnotation != null) {
				if (pAnnotation instanceof RegularExpressionValidation) {
					String regularExpression = ((RegularExpressionValidation)pAnnotation).expression();
					
					if (regularExpression != null && !regularExpression.trim().equals("")) {
						
						Pattern pattern = Pattern.compile(regularExpression);
						if (pattern != null) {
							String value = "";
							if (pValue instanceof String) {
								value = (String)pValue;
							} else {
								value = pValue.toString();
							}
							
							Matcher matcher = pattern.matcher(value);
							if (!matcher.matches()) {
								returnedValue = false;
							}
						}
					}
				}
			}
		}
		
		return returnedValue;
	}
}
