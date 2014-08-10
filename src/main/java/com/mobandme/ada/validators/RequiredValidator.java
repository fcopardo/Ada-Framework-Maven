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
import com.mobandme.ada.Entity;


/**
 * This class implement the logic to validate the required fields validations. 
 * @version 2.4.3
 * @author Mob&Me
 */
public final class RequiredValidator extends Validator {

	@Override
	public Boolean Validate(Entity pEntity, Field pField, Object pAnnotation, Object pValue) {
		Boolean returnedValue = true;
		
		
		if (pValue == null) {
			returnedValue = false;
		} else {
			if (pValue instanceof String) {
				if (((String)pValue).trim().equals("")) {
					returnedValue = false;
				} 
			}
		}
		
		return returnedValue;
	}
}
