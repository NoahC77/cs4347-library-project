import Option from "../components/option";
import Title from "../components/title";
import {useContext, useEffect, useState} from "react";
import {BaseUrl} from "../App";
import axios from "axios";
import Submit from "../components/submit";
import {toast} from "react-toastify";

function PoList(props) {
  const baseUrl = useContext(BaseUrl)
  const [pos, setPos] = useState([])
  useEffect(() => {
    Promise.all(props.po
      .map(po => po.supplied_item_id)
      .map(getSuppliedItem)).then((suppliedItems) => {
      setPos(suppliedItems.map((suppliedItem) => ({
          name: suppliedItem.item.item_name,
          quantity: suppliedItem.suppliedItem.quantity,
          price: suppliedItem.suppliedItem.vendor_price
        })
      ))
    })


  }, [])

  async function getSuppliedItem(supplied_item_id) {
    let data = (await axios.get(baseUrl + "/suppliedItem/" + supplied_item_id)).data;
    console.log(data)
    return data
  }

  async function submit() {
    try {
      await axios.post(baseUrl + "/addPurchaseOrder", {
        supplied_items: props.po,
        warehouse_id: props.warehouse,
      })
      toast.success("Success")
    } catch (e) {
      toast.error("Error")
    }
  }

  return (
    <>
      <Title>Auto PO</Title>

      {pos.map(po =>
        <Option text1={`${po.name}*${po.quantity}`} text2={`$${po.price}`} className2={"text-right"}/>
      )}
      <Submit onClick={submit}/>
    </>
  );
}

export default PoList;