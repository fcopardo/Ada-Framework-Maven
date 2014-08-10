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
import android.graphics.Bitmap.CompressFormat;

import com.mobandme.ada.Entity;

/**
 * Annotation to define DataBase table field information.
 * @version 2.4.3
 * @author Mob&Me
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableField {
	/**
	 * Define the field is Primary Key.
	 * @deprecated Please, don't use this attribute, It's only for internal purposes.
	 * @return
	 */
	@Deprecated()
	boolean isPrimaryKey() default false;
	
	/***
	 * Define if field value is unique in the table.
	 * @return
	 */
	public boolean unique() default false;
	
	
	/**
	 * Define the field name in the Database table.
	 * @return
	 */
	public String name() default "";
	
	/**
	 * Define the field datatype in the Database table.
	 * @return
	 */
	public int datatype() default Entity.DATATYPE_STRING;
	
	/**
	 * Define the field length in the Database table.
	 * @return
	 */
	public int maxLength() default 0;
	
	/**
	 * Define if the field is required in the Database table.
	 * @return
	 */
	public boolean required() default false;
	
	/**
	 * Define if the value of the field will be stored encrypted.
	 * @return
	 */
	public boolean encripted() default false;
	
	/**
	 * Define Bitmap compression to save into repository.
	 * @return
	 */
	public CompressFormat BitmapCompression() default CompressFormat.PNG; 
	
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
	 * Define if the field doesn't have storage persistence. This fields will be used to special operation for example as counting sentences.
	 * @return
	 */
	public boolean virtual() default false;
}
