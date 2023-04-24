import Option from '../components/option'
import Search from '../components/search'
import Field from '../components/field'
import Submit from '../components/submit'
import Title from '../components/title'

import {useState, createContext, useContext, useEffect} from 'react'
import {BaseUrl, Context} from '../App'
import EndpointField from "../components/endpointField";
import axios from "axios";
import {toast} from "react-toastify";

function AddRemove(props) {
  return (<div className="w-full flex gap-[2%] justify-center space-x-0">
    <div onClick={() => props.onAdd()} className="
          h-[5vh] w-[34%] bg-[#22BB88] rounded-[5px] mb-[5vh] px-[2%] [cursor:pointer]
          text-white text-[3vh] text-center
        ">
      +
    </div>
    <div onClick={() => props.onRemove()} className="
          h-[5vh] w-[34%] bg-[#C83A4A] rounded-[5px] mb-[5vh] px-[2%] [cursor:pointer]
          text-white text-[3vh] text-center
        ">
      -
    </div>
  </div>);
}

function AddPO(props) {
  const {page, setPage} = useContext(Context)
  const baseUrl = useContext(BaseUrl)
  const [suppliedItems, setSuppliedItems] = useState([{supplied_item_id: 1, quantity: 1}]);

  async function addPO() {
    console.log(suppliedItems);
    try {
      await axios.post(baseUrl + "/addPurchaseOrder", suppliedItems);
      toast.success("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Add Purchase Order</Title>

      {suppliedItems.map((item, index) => {
        return (<div key={index}>
          <EndpointField text1={"Supplied Item:"} endpoint={"/suppliedItems"}
                         transform={item => [item.suppliedItem.supplied_item_id, item.item.item_name]}
                         onValueChange={(newVal) => {
                           const newSuppliedItems = [...suppliedItems];
                           newSuppliedItems[index].supplied_item_id = newVal;
                           setSuppliedItems(newSuppliedItems);
                         }}/>
          <Field editable={true} text1="Quantity:" text2={item.quantity ?? 1}
                 onValueChange={(newVal) => {
                   const newSuppliedItems = [...suppliedItems];
                   newSuppliedItems[index].quantity = newVal;
                   setSuppliedItems(newSuppliedItems);
                 }}/>
        </div>)
      })}
      <AddRemove onAdd={() => {
        setSuppliedItems([...suppliedItems, {supplied_item_id: 1, quantity: 1}])
      }} onRemove={() => {
        setSuppliedItems(suppliedItems.slice(0, Math.max(1, suppliedItems.length - 1)))
      }}/>
      <Submit onClick={addPO}/>
    </>
  );
}

export default AddPO;

function resize(arr, size, defval) {
  while (arr.length > size) {
    arr.pop();
  }
  while (arr.length < size) {
    arr.push(defval);
  }
}