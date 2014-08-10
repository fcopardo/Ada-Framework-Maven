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

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Internal class dedicated to Database Schema management and Database connections management.
 * @version 2.4.3
 * @author Mob&Me
 */
class DataBaseHelper extends SQLiteOpenHelper {
	private ObjectContext context;
	private void setContext(ObjectContext pContext) {
		this.context = pContext;
	}
	private ObjectContext getContext() {
		return  this.context;
	}
	
	/**
	 * Principal constructor of the class.
	 * @param pContext
	 */
	public DataBaseHelper(ObjectContext pContext) {
		super(pContext.getContext(), pContext.getDatabaseFileName(), null, pContext.getDatabaseVersion());
		setContext(pContext);
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onCreate(SQLiteDatabase pDataBase) {
		try {
			
			getContext().onPreCreate(pDataBase);
			getContext().onCreateDataBase(pDataBase);
			getContext().onPostCreate(pDataBase);

			getContext().onPopulate(pDataBase);
			getContext().onPopulate(pDataBase, ObjectContext.ACTION_CREATE);
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(e.toString());
			getContext().onError(e);
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void onUpgrade(SQLiteDatabase pDataBase, int pOldVersion, int pNewVersion) {
		
		
		try {
			
			getContext().onPreUpdate(pDataBase, pOldVersion, pNewVersion);
			getContext().onUpdateDataBase(pDataBase, pOldVersion, pNewVersion);
			getContext().onPostUpdate(pDataBase, pOldVersion, pNewVersion);
			
			getContext().onPopulate(pDataBase);
			getContext().onPopulate(pDataBase, ObjectContext.ACTION_UPDATE);
			
		} catch (Exception e) {
			ExceptionsHelper.manageException(e.toString());
			getContext().onError(e);
		}
	}
}
