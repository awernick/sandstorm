
/**
 * Created by awernick on 1/13/15.
 */
public interface PropertyChangeListener
{
    public void propertyChange(PropertyChangeEvent changeEvent);

    public class PropertyChangeEvent<T>
    {
        Object source;
        String propertyName;
        T oldValue;
        T newValue;

        PropertyChangeEvent(Object source, String propertyName, T oldValue, T newValue )
        {
            this.source = source;
            this.propertyName = propertyName;
            this.oldValue = oldValue;
            this.newValue = newValue;
        }

        public Object getSource()
        {
            return source;
        }

        public String getPropertyName()
        {
            return propertyName;
        }

        public T getOldValue()
        {
            return oldValue;
        }

        public T getNewValue()
        {
            return newValue;
        }
    }
}


