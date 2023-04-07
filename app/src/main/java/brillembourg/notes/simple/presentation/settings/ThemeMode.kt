package brillembourg.notes.simple.presentation.settings

enum class ThemeMode(var type: String) : IsOption {
    Light("0"), Dark("1"), System("2");

    override fun getValue(): String {
        return type
    }

    override fun getName(): String {
        return desc
    }

    private val desc: String
        get() = when (this) {
            System -> "Follow system"
            Light -> "Light"
            Dark -> "Dark"
        }

    companion object {
        fun getThemeFromType(type: Int): ThemeMode {
            return when (type.toString()) {
                System.type -> {
                    System
                }
                Light.type -> {
                    Light
                }
                Dark.type -> {
                    Dark
                }
                else -> {
                    throw IllegalArgumentException("Type: $type is not a valid argument to create ThemeType")
                }
            }
        }
    }

}