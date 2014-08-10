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
 * DataBinding Structure.
 * @version 2.4.3
 * @author Mob&Me
 */
public class DataBinding {
	/**
	 * Define the Entity Field.
	 */
	public Field EntityField;
	
	/**
	 * Define the Getter Method of the Entity.
	 */
	public Method getterMethod;
	/**
	 * Define the Setter Method of the Entity.
	 */
	public Method setterMethod;
	/**
	 * Define the View Id into the UI layer.
	 */
	public int ViewId;
	
	/**
	 * Define the custom binder class type.
	 */
	public Class<?> Binder;
	
	/**
	 * Define the custom data parser class type.
	 */
	public Class<?> Parser;
	
	/**
	 * Default constructor of the class.
	 */
	public DataBinding() { }
	
	/**
	 * Secondary constructor of the class.
	 * @param pEntityField
	 * @param pViewId
	 */
	public DataBinding(Field pEntityField, int pViewId) {
		this.EntityField = pEntityField;
		this.ViewId = pViewId;
	}
	/**
	 * Secondary constructor of the class.
	 * @param pEntityField
	 * @param pViewId
	 * @param pGetterMethod
	 */
	public DataBinding(Field pEntityField, int pViewId, Method pGetterMethod) {
		this.EntityField = pEntityField;
		this.ViewId = pViewId;
		this.getterMethod = pGetterMethod;
	}
}
