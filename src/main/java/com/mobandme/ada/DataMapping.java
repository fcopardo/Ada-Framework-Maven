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
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

/**
 * Internal Entity fields DataMapping class.
 * @version 2.4.3
 * @author Mob&Me
 */
class DataMapping {
	public Class<?> EntityManagedType = null;
	public String EntityFieldName = "";
	public Field EntityManagedField = null;
	public String DataBaseLinkedTableName = "";
	public String DataBaseMiddleTableName = "";
	public String DataBaseTableName = "";
	public String DataBaseFieldName = "";
	public int DataBaseDataType = Entity.DATATYPE_STRING;
	public int DataBaseLength = 20;
	public int DataBaseColumnIndex = 0;
	public boolean DataBaseAllowNulls = true;
	public boolean DataBaseIsPrimaryKey = false;
	public boolean Encrypted = false;
	public boolean Unique = false;
	public boolean ForeignKey = false;
	public boolean IsCollection = false;
	public boolean IsSpecialField = false;
	public CompressFormat BitmapCompression = CompressFormat.PNG;
	public Method getterMethod = null;
	public Method setterMethod = null;
	public Boolean virtual = false;
}
