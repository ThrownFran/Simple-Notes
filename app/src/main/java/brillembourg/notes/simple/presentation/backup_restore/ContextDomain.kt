package brillembourg.notes.simple.presentation.backup_restore

import android.content.Context
import brillembourg.notes.simple.domain.use_cases.Screen

//Class used to hide Context in domain layer
class ContextDomain(val context: Context) : Screen