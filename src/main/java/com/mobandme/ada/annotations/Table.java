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


/**
 * Annotation to define information of the DataBase table.
 * @version 2.4.3
 * @author Mob&Me
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	/**
	 * Name of the DataBase Table.
	 * @return String
	 * @author DesAndrOId
	 */
	public String name() default "";
	
	/**
	 * Define if the table creation process generate indexes.
	 * @return
	 */
	public boolean useIndexes() default true;
}
