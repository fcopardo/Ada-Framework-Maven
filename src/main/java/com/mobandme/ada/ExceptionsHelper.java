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

import com.mobandme.ada.exceptions.AdaFrameworkException;
import android.app.Activity;
import android.util.Log;

/**
 * The class encapsulate the exceptions management.
 * @version 2.4.3
 * @author Mob&Me
 */
class ExceptionsHelper {
	/**
	 * Manage the framework exceptions.
	 * @param pException
	 * @throws Exception
	 */
	public static void manageException(final ObjectSet<?> pObjcetSet, final Exception pException) throws AdaFrameworkException {
		pException.printStackTrace();
		manageException(pException.toString());
		
		if (pObjcetSet != null) {
			if (pObjcetSet.getObjectSetEventsListener() != null) {
				if (pObjcetSet.getContext() != null) {
					if (pObjcetSet.getContext().getContext() != null) {
						if (pObjcetSet.getContext().getContext() instanceof Activity) {
							((Activity)pObjcetSet.getContext().getContext()).runOnUiThread(new Runnable() {
								
								@Override
								public void run() {
									pObjcetSet.getObjectSetEventsListener().OnError(pObjcetSet, pException);
								}
							});
						}
					}
				}
			}
		}

		if (pException instanceof AdaFrameworkException) {
			throw (AdaFrameworkException)pException;
		} else {
			throw new AdaFrameworkException(pException);
		}
	}
	
	/**
	 * Manage the framework exceptions.
	 * @param pException
	 * @throws Exception
	 */
	public static void manageException(String pException) {
		Log.e(DataUtils.DEFAULT_LOGS_TAG, pException.toString());
	}
	
	/**
	 * Manage the framework exceptions.
	 * @param pException
	 * @throws Exception
	 */
	public static void manageException(Exception pException) throws AdaFrameworkException {
		manageException(pException.getMessage());
		throw new AdaFrameworkException(pException);
	}
}
