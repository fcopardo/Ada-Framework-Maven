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
 * InaccessibleObjectSetException exception.
 * @version 2.4.3
 * @author Mob&Me
 */
@SuppressWarnings("serial")
public class InaccessibleObjectSetException extends AdaFrameworkException {

	private String objectContextName = "";
	private String objectSetName = "";
	
	public String getObjectContextName() { return objectContextName; }
	public String getObjectSetName() { return objectSetName; }
	
	public InaccessibleObjectSetException(String pObjectContextName, String pObjectSetName) { 
		super(generateMessage(pObjectContextName, pObjectSetName));
		
		objectContextName = pObjectContextName;
		objectSetName = pObjectSetName;
	}
	
	@Override
	public String toString() {
		return generateMessage(objectContextName, objectSetName);
	}
	
	private static String generateMessage(String pObjectContextName, String pObjectSetName) {
		return String.format("The ObjectSet '%s' of the ObjectContext '%s' is not accesible. Please change the field modifier or create a getter method.", pObjectSetName, pObjectContextName);
	}
}
