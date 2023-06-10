namespace('planningPoker.Viewtility',{},() => {
    const buildTextField = function(label, fieldId, style, getter, setter) {
        return <div className="input-group">
            <label htmlFor={fieldId} className="input-group-text">{label}</label>
            <input id={fieldId}
                   type="text"
                   className="form-control"
                   style={style || {}}
                   value={ getter() }
                   onChange={(e) => setter(e.target.value)}
                />
        </div>;
    }
    return { buildTextField };
});