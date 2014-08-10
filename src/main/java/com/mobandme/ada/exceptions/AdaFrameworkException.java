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
 * Framework generic exception.
 * @version 2.4.3
 * @author Mob&Me
 */
@SuppressWarnings("serial")
public class AdaFrameworkException extends Exception { 
	
	private Exception _innerException;
	void setInnerException(Exception pException) {
		this._innerException = pException;
	}
	public Exception getInnerException() {
		return this._innerException;
	}
	
	private String _message = "";
	public String getMessage() {
		return _message;
	}
	
	
	public void setMessage(String _message) {
		this._message = _message;
	}
	
	public AdaFrameworkException(Exception pInnerException) {
		if (pInnerException != null)
			setMessage(pInnerException.getMessage());
		
		setInnerException(pInnerException);
	}
	
	public AdaFrameworkException(String pMessage) {
		super(pMessage);
		
		setMessage(pMessage);
	}
	public AdaFrameworkException(String pMessage, Exception pInnerException) {
		super(pMessage, pInnerException);
		
		setMessage(pMessage);
		setInnerException(pInnerException);
	}
}
