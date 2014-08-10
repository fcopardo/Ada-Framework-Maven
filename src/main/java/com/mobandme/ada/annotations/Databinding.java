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

package com.mobandme.ada.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.mobandme.ada.DataBinder;
import com.mobandme.ada.DataParser;

/**
 * Annotation to define data binding between the Entity object and the UI View control.
 * @version 2.4.3
 * @author Mob&Me
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Databinding {
	/**
	 * Define the View control ID linked to the Entity property.
	 * @return
	 */
	public int ViewId();
	
	/**
	 * Define the custom property getter suffix, Example: getXXXX() => getMySufix()
	 * @return
	 */
	public String getterSuffix() default "";
	
	/**
	 * Define the custom property setter suffix, Example: setXXXX(Object pVar) => setMySufix(Object pVar)
	 * @return
	 */
	public String setterSuffix() default "";
	
	/**
	 * Define the custom DataBinder Class.
	 * @return
	 */
	public Class<?> binder() default DataBinder.class;
	
	
	/**
	 * Define the custom dapa parser class.
	 * @return
	 */
	public Class<?> parser()  default DataParser.class;
}
