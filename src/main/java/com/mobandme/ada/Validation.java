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


package com.mobandme.ada;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Define the internal structure of validation.
 * @version 2.4.3
 * @author Mob&Me
 */
final class Validation {

	public Object Annotation;
	
	/**
	 * Define the Entity Field.
	 */
	public Field EntityField;
	
	/**
	 * Define the error message;
	 */
	public String message;
	
	/**
	 * Define the Getter Method of the Entity.
	 */
	public Method getterMethod;
	
	/**
	 * Define the custom binder class type.
	 */
	public Class<?> Validator;
	
	
}
