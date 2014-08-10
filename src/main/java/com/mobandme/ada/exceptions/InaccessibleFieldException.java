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

package com.mobandme.ada.exceptions;

/**
 * InaccessiblePropertyException exception.
 * @version 2.4.3
 * @author Mob&Me
 */
@SuppressWarnings("serial")
public class InaccessibleFieldException extends AdaFrameworkException {

	private String entityName = "";
	private String propertyName = "";
	private String methodName = "";
	
	public String getEntityName() { return entityName; }
	public String getFieldName() { return propertyName; }
	public String getMethodName() { return methodName; }
	
	public InaccessibleFieldException(String pEntity, String pProperty, String pMethod) { 
		super(generateMessage(pEntity, pProperty, pMethod));
		entityName = pEntity;
		propertyName = pProperty;
		methodName = pMethod;
	}
	
	@Override
	public String toString() {
		return generateMessage(entityName, propertyName, methodName);
	}
	
	private static String generateMessage(String pEntity, String pProperty, String pMethod) {
		
		if (pMethod != null && !pMethod.trim().equals("")) {
			return String.format("Inaccessible Field on Entity %s and Property %s, Method Name: %s.", pEntity, pProperty, pMethod);
		} else {
			return String.format("Inaccessible Field on Entity %s and Property %s.", pEntity, pProperty);	
		}
	}
}
