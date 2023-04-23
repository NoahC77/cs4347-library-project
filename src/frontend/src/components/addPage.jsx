import {useContext, useState} from "react";
import {BaseUrl} from "../App";
import axios from "axios";
import Title from "./title";
import Field from "./field";
import Submit from "./submit";
import {toast} from "react-toastify";

/**
 * @readonly
 * @enum {string}
 */
const FieldType = {
  STRING: 'string',
  INT: 'integer',
  HIDDEN: 'hidden'
}

/**
 * @typedef {Object} AddPageField
 * @property {string} key
 * @property {string} [label]
 * @property {string | number} [defaultValue]
 * @property {FieldType} type
 */

/**
 * @typedef {Object} AddPageProps
 * @property {string} title
 * @property {string} endpoint
 * @property {AddPageField[]} fields
 */

/**
 * @param {AddPageProps} props
 * @param props
 */
function AddPage(props) {
  const baseUrl = useContext(BaseUrl)
  const [fields, setFields] = useState(props.fields
    .filter((field) => field.type !== FieldType.HIDDEN)
    .map((field) => {
      return {
        ...field,
        value: field.type === FieldType.INT ? 0 : '',
      }
    }))

  async function addEntry() {
    let entry = fields.reduceRight((acc, field) => ({...acc, [field.key]: field.value}), {})
    entry = {
      ...entry, ...props.fields.filter((field) => field.type === FieldType.HIDDEN)
        .reduceRight((acc, field) => ({...acc, [field.key]: field.defaultValue}), {})
    }
    try {
      await axios.post(baseUrl + props.endpoint, entry)
      toast.success("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>{props.title}</Title>
      {fields.map((field, index) =>
        <Field editable={true} key={field.key} text1={`${field.label}:`} text2={field.value} onValueChange={(value) => {
          const newFields = [...fields]
          newFields[index].value = field.type === FieldType.INT ? parseInt(value) : value
          setFields(newFields)
        }}/>
      )}
      <Submit onClick={addEntry}>Add</Submit>
    </>
  )
}

export {AddPage, FieldType}