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

package com.mobandme.ada.listeners;

import com.mobandme.ada.ObjectSet;

/***
 * Define the events fired by the ObjectSets.
 * @version 2.4.3
 * @author Mob&Me
 */
public interface ObjectSetEventsListener {
	/**
	 * Occurs when the Fill process of ObjectSet finalize.
	 */
	void OnFillComplete(ObjectSet<?> pObjsetSet);
	
	/**
	 * Occurs when the Save process of ObjectSet finalize.
	 * @param pObjsetSet
	 */
	void OnSaveComplete(ObjectSet<?> pObjsetSet);
	
	/**
	 * Occurs when framework process fail.
	 * @param pObjcetSet
	 * @param pException
	 */
	void OnError(ObjectSet<?> pObjcetSet, Exception pException);
	
	/**
	 * Occurs during the save process.
	 * @param pObjsetSet
	 * @param pActualPosition
	 * @param pTotalNumOfPositions
	 */
	void OnSaveProgress(ObjectSet<?> pObjsetSet, int pActualPosition, int pTotalNumOfPositions);
}
